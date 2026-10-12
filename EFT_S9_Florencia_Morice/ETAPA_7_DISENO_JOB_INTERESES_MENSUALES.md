# Etapa 7 - Diseño del job de cálculo mensual de intereses

## 1. Alcance y restricciones

Esta etapa define el diseño del segundo proceso Spring Batch en `batch/batch-jobs`. No implementa el job, no calcula intereses, no altera saldos y no marca registros como financieramente aceptados. El cálculo queda bloqueado hasta que las reglas financieras y la semántica de la fuente sean aprobadas.

No se modifican Semana 8, los CSV originales ni el job `movimientosReporteDiarioJob`. Tampoco se ejecuta ningún job ni se sobrescriben artefactos de salida.

## 2. Evidencia revisada

Se revisaron las documentaciones de las Etapas 3, 4 y 5, `README.md`, los POM raíz y del módulo `batch/batch-jobs`, la estructura de arranque batch y el CSV real `data/semana_3/intereses_trimestrales.csv`.

La plataforma usa Java 17, Spring Boot 3.1.6, Spring Cloud 2022.0.5 y Spring Batch administrado por Spring Boot. El POM del módulo contiene `spring-boot-starter-batch`, JDBC, H2 para metadata local y `spring-boot-starter-test` para JUnit. La configuración actual de arranque lanza explícitamente solo `movimientosReporteDiarioJob`; esta etapa no agrega un segundo launcher que pudiera ejecutar jobs conjuntamente.

El CSV tiene 1.000 filas y estas columnas: `cuenta_id,nombre,saldo,edad,tipo`. Los conteos verificados son:

| Hallazgo | Cantidad |
|---|---:|
| `saldo` vacío | 204 |
| `edad` vacía | 171 |
| Edad no vacía fuera del intervalo exploratorio 0-120 de la Etapa 5 | 46 |
| `tipo=-1` | 281 |
| `tipo=unknown` | 44 |
| `tipo=ahorro` | 236 |
| `tipo=prestamo` | 236 |
| `tipo=hipoteca` | 203 |
| `cuenta_id` distintos | 50 |
| IDs de cuenta repetidos | 50 |
| Filas adicionales sobre la primera aparición de cada cuenta | 950 |
| Filas completas duplicadas | 7 |
| Cuentas que aparecen asociadas a más de un nombre | 50 |
| Saldos no vacíos no numéricos, negativos o iguales a cero | 0 |

La fuente no incluye fecha de corte, año/trimestre, período de vigencia, tasa, moneda, precisión monetaria ni identificador de fila de negocio. El nombre `intereses_trimestrales.csv` no demuestra por sí solo si cada fila es un saldo trimestral, un historial, una tasa trimestral o una base para producir períodos mensuales.

## 3. Diferencia entre la fuente trimestral y el requisito mensual

El requisito es producir un resultado mensual. La fuente se denomina trimestral, pero solo trae cuenta, nombre, saldo, edad y tipo; no tiene un período que permita ubicar cada fila en un trimestre, ni una tasa o fórmula que determine un importe de interés. Tampoco permite inferir si `saldo` es inicial, promedio, final o un valor de referencia.

Por tanto, no existe una conversión trimestral-a-mensual determinista basada en este archivo. Dividir una tasa trimestral por tres, capitalizarla, interpolar saldos o repetir el saldo trimestral en tres meses serían políticas financieras inventadas y quedan expresamente descartadas.

## 4. Decisiones financieras y de datos pendientes

Antes de habilitar el paso de cálculo, el negocio debe documentar y aprobar:

1. **Semántica de la fuente:** qué representa cada fila; si es snapshot o historial; trimestre/año de referencia; fecha de vigencia; y cómo se relaciona con el período mensual solicitado.
2. **Período de cálculo:** mes de proceso, límites y zona/fecha de corte; tratamiento de altas, cierres y movimientos dentro del mes.
3. **Tasa:** valor o tabla oficial; si varía por producto, tipo, edad, tramo de saldo u otro criterio; y si es nominal o efectiva y anual, mensual o trimestral.
4. **Fórmula y base:** interés simple o compuesto, saldo base (inicial, promedio diario, cierre u otro), prorrateo por días, capitalización y tratamiento de cambios de saldo. No hay fórmula propuesta en esta etapa.
5. **Redondeo:** moneda, escala, modo de redondeo y momento de aplicación (por fila, cuenta o agregado); regla de conciliación de residuos.
6. **Saldos:** si faltantes, cero o negativos se rechazan, retienen, corrigen desde otra fuente o se permiten según producto. El CSV observado no contiene saldos no vacíos negativos ni cero, pero sí 204 vacíos.
7. **Edad:** si participa en una regla de tasa; fecha a la que se calcula; si es obligatoria; y rango permitido. El intervalo 0-120 usado para el análisis exploratorio no es una política aprobada. Hay 171 vacíos y 46 valores mayores de 120.
8. **Tipos:** catálogo oficial y significado de `ahorro`, `prestamo`, `hipoteca`, `-1` y `unknown`; reglas de normalización. No se debe convertir `-1` o `unknown` a un producto ni alterar silenciosamente el catálogo.
9. **Cuentas repetidas:** clave natural y cardinalidad esperada; si las repeticiones son historial, períodos diferentes, productos múltiples o errores; resolución de múltiples nombres por cuenta y tratamiento de las 7 filas completas duplicadas. No elegir una fila, descartar ni sumar repetidos automáticamente.
10. **Errores y recuperabilidad:** qué errores detienen todo el job, cuáles van a cuarentena y cuáles permiten continuar; límite de errores, reintentos, política de reinicio/idempotencia y autorización de resultados parciales.
11. **Aceptación y auditoría:** quién aprueba tasas/reglas, versión efectiva de la política, evidencia a conservar, retención de datos personales y condiciones para considerar el resultado contabilizable.

## 5. Arquitectura propuesta

Job propuesto: `interesesMensualesJob`, separado del job diario de movimientos. No debe ejecutar ningún cálculo sin un parámetro o configuración de política aprobada, versionada y efectiva para el mes solicitado. Un parámetro ausente o no aprobado debe detener el flujo antes de producir resultados financieros.

### 5.1 Prevalidación de entrada

- Exigir archivo presente y legible, encabezado exacto y columnas esperadas.
- Capturar checksum del archivo, nombre, cantidad de filas y parámetros del job (`periodoMensual`, ruta, identificador de ejecución y, en el futuro, versión de reglas aprobadas).
- No inferir el mes desde el nombre del archivo ni desde el orden de las filas.
- No escribir sobre el CSV fuente.

### 5.2 Lectura y validación técnica

- `FlatFileItemReader<InteresTrimestralCsv>` con número de línea física y mapeo explícito de las cinco columnas.
- Validar campos requeridos, formato decimal de `saldo`, entero de `edad`, encoding, columnas y errores de parseo.
- Emitir clasificación técnica separada de la decisión financiera. Los faltantes o valores incompatibles bloquean el cálculo; su destino final (rechazo o revisión) depende de la política aprobada.
- Elaborar diagnóstico global de frecuencia por `cuenta_id`, IDs con nombres diferentes y filas completamente repetidas. La duplicidad es candidata a revisión; no se deduplica ni consolida sin clave y política aprobadas.

### 5.3 Procesamiento mensual, condicionado

- Mantener separados validador técnico, clasificador de registros que requieren revisión y calculador financiero.
- El calculador futuro debe recibir una política aprobada e identificable por versión; no debe contener tasas, fórmula, selección de saldo, reglas de edad o mapeos de tipo implícitos.
- En esta etapa no se crea ni se invoca una implementación de cálculo. Con las decisiones pendientes, la salida calculada debe permanecer deshabilitada.

### 5.4 Salida

Cuando se autorice implementar y ejecutar, usar un directorio aislado por período y ejecución, por ejemplo `batch/batch-jobs/output/intereses-mensuales/<periodo>/<execution-id>/`, nunca la ruta del primer job ni el CSV original.

Diseño de artefactos:
- `intereses_calculados.csv`: solo se habilita después de aprobar y versionar las reglas; no se crea como resultado financiero en esta etapa.
- `intereses_revision.csv`: filas sin semántica resuelta, duplicados candidatos y casos que requieren decisión humana.
- `intereses_rechazados.csv`: filas que incumplen validaciones técnicas confirmadas, con número de línea y motivo.
- `intereses_resumen.txt`: cantidades conciliadas, fuente/checksum, período, política/versiones, errores y estados del job.

Evitar propagar `nombre` a salidas que no lo necesiten. CSV de errores debe escapar comillas, delimitadores y saltos de línea.

### 5.5 Auditoría y conciliación

Contabilizar categorías mutuamente excluyentes para cada fila fuente, por ejemplo: calculada bajo reglas aprobadas, revisión pendiente, rechazo técnico o error de lectura no recuperable. Cualquier subcontador (por ejemplo, cuenta repetida dentro de revisión) debe declararse como subconjunto y no sumarse de nuevo al total.

Condición de cierre propuesta: `filas_fuente = calculadas + revisión + rechazadas + errores_no_recuperables`, con `conciliacion_ok` explícito. Registrar `JobExecution`/`StepExecution`, checksum, período, versión de regla, tiempos, conteos de lectura/escritura/skip y causa de fallo. No presentar un job `COMPLETED` como aprobación financiera; la aprobación debe ser un estado independiente con responsable y evidencia.

## 6. Casos de prueba documentados

Estos escenarios definen comportamiento esperado para la futura implementación. No son tests ejecutables todavía y no calculan importes.

| Caso | Datos/escenario | Resultado esperado antes de calcular |
|---|---|---|
| Fila estructuralmente válida | Cuenta única para el fixture, nombre presente, saldo decimal, edad entera y tipo de catálogo aprobado; período y política explícitos | Pasa validación técnica. Sin fórmula aprobada no se invoca calculador ni se escribe interés; no se etiqueta como aceptada financieramente. |
| Datos incompletos | `saldo` vacío (presente en la fuente) o `edad` vacía; fixture adicional con identificador faltante | Registrar línea y campo. No calcular. Mandar a revisión/rechazo según obligatoriedad aprobada; no reemplazar valores. |
| Edad fuera de rango | Edad 150, observada en la fuente; usar también un valor limítrofe en pruebas cuando negocio defina el rango | Detectar y registrar. No aplicar tasa por edad ni rechazar definitivamente hasta confirmar si la edad participa y el rango válido. |
| Tipo desconocido | `-1` o `unknown`, ambos presentes en la fuente | No mapear a `ahorro`, `prestamo` ni `hipoteca`; registrar tipo original y dejar en revisión, sin calcular. |
| Cuenta repetida | Dos o más filas con el mismo `cuenta_id`; variantes de nombre; duplicado completo | Preservar líneas de origen, reportar frecuencia y candidato duplicado. No descartar, fusionar, sumar saldos ni seleccionar una fila sin regla aprobada. |
| Saldo con formato ilegible | Decimal sintético no parseable | Error técnico asociado a la línea; no calcular ni convertir silenciosamente. |
| Fuente sin período mensual asociable | CSV actual, que no contiene año/trimestre o fecha de vigencia | Prevalidación bloquea cálculo; no inferir período por nombre ni por posición. |
| Error de lectura/escritura | Fila CSV mal formada o fallo de salida en entorno temporal de prueba | Registrar categoría, línea disponible y causa; verificar reconciliación y política de fallo. No reutilizar la carpeta de movimientos. |
| Política no aprobada | Falta tasa, fórmula, versión o vigencia para el mes | Job no inicia el paso financiero y no produce `intereses_calculados.csv`. |

## 7. Dependencias con el primer job

No se detectó una dependencia imprescindible del job de movimientos. La configuración actual de `BatchJobsApplication` lanza explícitamente `movimientosReporteDiarioJob`; no se debe añadir un segundo `ApplicationRunner` que ejecute intereses automáticamente al arrancar la aplicación. Cuando exista autorización de implementación, el lanzamiento del segundo job deberá ser explícito y seleccionable, evitando ejecutar o sobrescribir las salidas del primero.

## 8. Archivos modificados y estado

- `ETAPA_7_DISENO_JOB_INTERESES_MENSUALES.md` (este documento).

No se modificaron Java, POM, CSV originales, outputs existentes ni Semana 8. No se añadieron ni ejecutaron pruebas en esta etapa de diseño; los casos quedan especificados para una fase de implementación autorizada.

## 9. Confirmaciones necesarias antes de implementar

- significado y período de cada fila trimestral, y mes objetivo;
- tasa, base, fórmula, periodicidad efectiva y capitalización;
- moneda, precisión y redondeo;
- obligatoriedad y reglas de saldo y edad;
- catálogo y semántica de tipos;
- claves y tratamiento de cuentas/filas repetidas;
- política de errores, reintentos, parciales e idempotencia;
- aprobador y versión de las reglas que habilitan resultados financieros.

La Etapa 7 queda detenida en diseño. No se implementa la fórmula ni se ejecuta el segundo job.
