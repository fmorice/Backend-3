# Etapa 8 - Diseño del job de estados financieros anuales

## 1. Alcance y restricciones

Esta etapa documenta el tercer job Spring Batch propuesto para `batch/batch-jobs`. No implementa clases ni reglas, no calcula o consolida importes, no crea estados aceptados y no ejecuta jobs.

Se trabajó solo con documentación y datos de Semana 9. No se modifican Semana 8, los CSV originales, los artefactos existentes ni los jobs previos. El job de movimientos no se vuelve a ejecutar y el de intereses continúa solamente en diseño.

## 2. Evidencia revisada

Se revisaron `README.md`, las Etapas 3, 4, 5 y 7, el POM raíz, el POM de `batch/batch-jobs`, la configuración actual de arranque batch y el CSV real `data/semana_3/estados_financieros_anuales.csv`.

El proyecto usa Java 17, Spring Boot 3.1.6 y Spring Batch administrado por Spring Boot. El módulo batch ya declara `spring-boot-starter-batch`, JDBC, H2 para metadata local y `spring-boot-starter-test`. El README identifica tres jobs independientes dentro de `batch/batch-jobs`. La configuración presente deshabilita el auto-lanzamiento de Spring Batch y `BatchJobsApplication` tiene un `ApplicationRunner` que selecciona explícitamente el primer job, `movimientosReporteDiarioJob`. El tercer job no debe conectarse a ese runner ni arrancar automáticamente al iniciar la aplicación.

## 3. Análisis real del CSV

El archivo tiene 1.000 filas de datos y el encabezado exacto:

`cuenta_id,fecha,transaccion,monto,descripcion`

| Indicador | Conteo observado |
|---|---:|
| `cuenta_id` vacío | 0 |
| `fecha` vacía | 0 |
| `transaccion` vacía | 0 |
| `monto` vacío | 48 |
| `descripcion` vacía | 184 |
| Montos no numéricos entre los no vacíos | 0 |
| Montos negativos | 232 |
| Montos iguales a cero | 116 |
| Montos positivos | 604 |
| Filas completas duplicadas | 0 |
| IDs de cuenta distintos | 20 |
| IDs de cuenta con más de una fila | 20 |
| Filas adicionales sobre la primera fila por cuenta | 980 |
| Máximo de filas observadas para una cuenta | 65 |
| Claves repetidas `(cuenta_id, fecha, transaccion)` candidatas a revisión | 6 |

Los cuatro formatos encontrados son `yyyy/MM/dd` (276 filas), `yyyy-MM-dd` (226), `dd/MM/yyyy` (258) y `dd-MM-yyyy` (240). En las 1.000 fechas no vacías, cada valor es válido bajo al menos una de esas interpretaciones; 821 tienen una sola interpretación válida y 179 admiten tanto día/mes como mes/día con resultados diferentes. Las 821 fechas interpretables sin ambigüedad corresponden a 2024. La fecha objetivo del estado anual no está incluida como parámetro ni se debe inferir del reloj del sistema.

Transacciones literales: `compra` (327), `retiro` (278), `deposito` (293), `pago` (51) y `depósito` (51). La diferencia entre `deposito` y `depósito` no se normaliza automáticamente. Las descripciones son texto libre; por ejemplo, el texto no debe usarse para reinterpretar signo o tipo de una transacción.

### 3.1 Discrepancia documental de fechas

La Etapa 5 informa 197 fechas ambiguas para este archivo. Al recalcular con `csv` y fechas estrictas, clasificando como ambigua una fecha no ISO que puede interpretarse válidamente como D/M/A y M/D/A con resultados distintos, obtuve 179 ambiguas y ninguna inválida bajo los cuatro formatos anteriores. La diferencia de 18 debe reconciliarse revisando la definición o el procedimiento utilizado en la Etapa 5 antes de fijar el conteo como criterio de aceptación. Los 179 casos son una clasificación técnica reproducible con el criterio expresado aquí, no una decisión de negocio sobre qué orden de fecha es correcto.

Las seis claves repetidas por cuenta-fecha-transacción no equivalen a seis duplicados probados: no hay identificador de transacción y distintas operaciones legítimas podrían compartir esos campos. El análisis encontró cero filas completamente idénticas. No deduplicar por cuenta ni por una clave tentativa sin definición aprobada.

## 4. Brecha entre el requisito y la fuente

El requisito menciona generar “estados financieros anuales”, mientras que la fuente observada es un detalle de transacciones. Tiene fechas y montos por movimiento, pero no declara ejercicio contable, moneda, saldo inicial, saldo de cierre, reglas de clasificación, catálogo firmado, ni qué totales debe contener el estado.

La existencia de fechas en 2024 no autoriza a asumir que el período solicitado sea 2024 o que cada cuenta deba tener un único estado anual. Tampoco se puede determinar si el resultado esperado es un detalle anual, un resumen por cuenta, un balance con saldos, un estado de resultados u otro artefacto. Esta brecha requiere precisión de evaluación/negocio antes de fijar contrato de salida o de calcular totales.

## 5. Decisiones pendientes

Antes de implementar procesamiento o dar por aceptado cualquier resultado, confirmar:

1. **Propósito y producto de salida:** definición de “estado financiero anual”; campos, nivel de detalle, destinatarios y si requiere detalle, resumen, balance u otro formato.
2. **Período:** ejercicio objetivo, calendario fiscal, límites de fecha, zona horaria y tratamiento de ejercicios incompletos. La ejecución deberá recibir el período explícitamente.
3. **Agrupación:** si agrupar por cuenta y ejercicio, cuenta-producto, cliente u otra clave; y cómo se tratan cuentas con operaciones en varias categorías.
4. **Fechas:** formato fuente oficial, cómo interpretar las 179 fechas D/M/A–M/D/A ambiguas y zona/fecha de contabilización. No elegir una interpretación silenciosamente.
5. **Semántica de transacciones:** catálogo permitido, dirección contable, mapeo y normalización de `deposito`/`depósito`, `compra`, `retiro` y `pago`.
6. **Signos y ceros:** significado de los 232 montos negativos y 116 ceros por tipo; reversas, anulaciones, devoluciones u otros casos. No invertir signos ni excluirlos automáticamente.
7. **Saldos y contabilidad:** si el estado requiere saldos iniciales/finales, de qué fuente salen y cómo se concilian; el CSV no contiene estos saldos.
8. **Moneda y redondeo:** moneda, escala, modo de redondeo y momento de aplicación. No hay moneda declarada.
9. **Descripción:** si es obligatoria, informativa u opcional; hay 184 vacías. No generar una descripción por inferencia.
10. **Duplicados:** identificador estable y clave natural de transacción; significado de las seis claves candidatas repetidas; regla para registros repetidos y operaciones con misma fecha/tipo.
11. **Errores:** cuáles provocan rechazo técnico, revisión manual o detención del job; tolerancia, reintentos, errores de lectura/escritura y política de resultados parciales.
12. **Aprobación y auditoría:** propietario de las reglas, versión efectiva, trazabilidad requerida y criterios para llamar al archivo final “aceptado” o “contabilizado”.

Hasta resolver estos puntos, un registro que pasa verificaciones de formato solo puede describirse como técnicamente validado; no como movimiento conciliado, saldo correcto o estado financieramente aprobado.

## 6. Arquitectura propuesta para Spring Batch

Job independiente propuesto: `estadosFinancierosAnualesJob`. Debe requerir parámetros explícitos como `ejercicio`, `inputPath`, `outputDir` único y versión de especificación aprobada cuando exista. La ausencia de ejercicio, interpretación de fecha o configuración aprobada debe impedir cualquier consolidación financiera.

### 6.1 Prevalidación

- Verificar existencia/lectura del archivo, encabezado y cantidad de columnas.
- Capturar checksum, ruta, período pedido, `JobExecution` y un identificador único de ejecución.
- Validar que el directorio de salida de esa ejecución no exista; no reutilizar el directorio de movimientos ni borrar resultados anteriores.
- Comprobar que hay contrato aprobado para ejercicio, fecha, grupos y salida. Sin ese contrato, detener antes del paso de agregación.

### 6.2 Lectura

- `FlatFileItemReader<EstadoFinancieroAnualCsv>` con mapeo explícito y número de línea física.
- Conservar por fila los datos fuente sin mutar el archivo original.
- Registrar fallo de parseo con archivo, línea cuando esté disponible y causa técnica.

### 6.3 Validación técnica

- Verificar cuenta, fecha, transacción, formato numérico y descripción según el contrato que se confirme.
- Diferenciar fecha imposible de fecha ambigua. Las ambiguas requieren interpretación aprobada; no se deben resolver solo por elegir un formatter.
- Clasificar vacío/no numérico como hallazgo de calidad sin sustituir por cero ni por texto inventado.
- Detectar duplicados completos y claves candidatas como informes separados; no suprimir registros automáticamente.
- Separar estados técnicos (`válido`, `rechazado`, `revisión`) de cualquier aprobación financiera.

### 6.4 Procesamiento condicionado

Una vez aprobado el contrato, procesar/ordenar transacciones por la clave y período acordados y producir el tipo de estado definido. El procesamiento debe mantener el detalle fuente y aplicar solo mapeos y fórmulas aprobados, versionados y testeables. No se implementa ahora la agregación ni un cálculo de saldos.

Para evitar agregar sobre una entrada desordenada o cargar todos los registros en memoria, considerar una etapa de staging/auditoría por fila y una etapa separada de agrupación; decidir almacenamiento, ordenamiento y clave de staging durante implementación, según el volumen y la persistencia autorizada.

### 6.5 Escritura

En una ejecución autorizada, usar un directorio exclusivo, por ejemplo `batch/batch-jobs/output/estados-anuales/<ejercicio>/<execution-id>/`, y fallar antes de escribir si ya existe. No compartir la ruta de salida del primer job ni la ruta del job de intereses.

Nombres propuestos, sujetos al formato de evaluación:
- `estados_anuales_detalle.csv` o `estados_anuales_resumen.csv`: escoger solo tras confirmar el artefacto esperado.
- `estados_anuales_rechazados.csv`: errores técnicos con línea y motivo.
- `estados_anuales_revision.csv`: fechas ambiguas, duplicados candidatos y decisiones pendientes.
- `estados_anuales_auditoria.txt`: período, checksum, reglas/versiones, contadores y resultado de conciliación.

Evitar añadir identificadores personales innecesarios. Escapar correctamente delimitadores, comillas y saltos de línea.

### 6.6 Auditoría y conciliación

Definir categorías mutuamente excluyentes por cada fila leída, por ejemplo: incluida en detalle/resumen, revisión pendiente, rechazo técnico o error de lectura no recuperable. Conciliar **filas de entrada**, no el número de estados agregados por cuenta:

`filas_fuente = filas_incluidas + filas_en_revision + filas_rechazadas + errores_de_lectura_no_recuperables`

Los errores de escritura deben registrarse de manera diferenciada; si una escritura falla, el job no debe reportar éxito ni publicar una salida parcial como final. Registrar `readCount`, skips por lectura/proceso/escritura, filas escritas, cantidad de cuentas/grupos emitidos y reconciliación explícita. Los subtotales monetarios solo se concilian cuando el negocio defina la semántica de signos y agregación; no calcular totales de control ahora.

## 7. Casos de prueba documentados

Estos son escenarios para implementar y ejecutar en una etapa posterior. No se agregan tests ejecutables ni se usa el CSV real como destino de salida.

| Caso | Datos/escenario | Resultado esperado del diseño |
|---|---|---|
| Registro válido | Cuenta presente, fecha inequívoca y válida, transacción del catálogo aprobado, monto numérico y descripción presente | Pasa validación técnica. Sin agrupación/reglas aprobadas no se genera ni se marca un estado anual aceptado. |
| Fecha inválida | Fecha imposible sintética, por ejemplo `2024-02-30` | Rechazo técnico con número de línea y motivo; no se reemplaza ni se procesa. |
| Fecha ambigua | Fecha fuente como `02-01-2024` o `11/03/2024` | Retener para revisión según criterio aprobado; no escoger D/M/A o M/D/A automáticamente. |
| Monto vacío | Fila con `monto` vacío, como los 48 casos reales | Registrar campo faltante y línea; no sustituir por cero ni incluir en totales hasta definir tratamiento. |
| Descripción faltante | Fila con `descripcion` vacía, como los 184 casos reales | Registrar hallazgo. Rechazo o aceptación técnica depende de si negocio define descripción obligatoria; no inventar texto. |
| Monto negativo | Monto negativo de la fuente, incluyendo 232 observados | Preservar signo y tipo; no invertir, descartar ni tratar como reversa sin regla aprobada. |
| Monto cero | Monto cero de la fuente, incluyendo 116 observados | Preservar y reportar; no tratar como ausencia ni excluir sin política confirmada. |
| Cuenta repetida | Varias filas para el mismo `cuenta_id`, incluso las seis claves cuenta-fecha-transacción repetidas | Mantener trazabilidad por línea y reportar frecuencia; no deduplicar, agrupar o sumar sin contrato de clave y período. |
| Error de lectura | CSV con fila mal formada en un archivo temporal de prueba | Registrar línea/categoría/causa cuando esté disponible; verificar que el conteo de filas y errores concilia. |
| Error de escritura | Destino temporal no escribible o fallo simulado | Job falla; conservar auditoría, no marcar salida parcial como final y no modificar ejecuciones anteriores. |
| Salida ya existente | Directorio asociado a un `execution-id` ocupado | Fallo de prevalidación sin truncar ni eliminar artefactos. |
| Conteos reconciliados | Fixtures con válidos, revisión, rechazados y un error de lectura | Cada fila fuente pertenece a una sola categoría primaria y la ecuación de conciliación se cumple; subcontadores se identifican como subconjuntos. |

## 8. Contradicciones y dependencias

- **Requisito frente a datos:** el requisito dice “estado anual”, pero el archivo solo documenta movimientos; no define saldos ni el contenido del estado final. Se necesita aclarar el producto esperado antes de la agregación.
- **Conteo de fechas:** Etapa 5 reporta 197 ambiguas; el análisis reproducible de esta etapa encuentra 179 ambiguas bajo el criterio de dos interpretaciones válidas y cero fechas inválidas bajo los cuatro formatos observados. Se documenta la diferencia; no se modifica Etapa 5 en esta etapa.
- **Lanzamiento:** la aplicación actual tiene un runner explícito para el primer job. El tercer job requerirá en el futuro un selector/lanzamiento explícito. No hay dependencia que obligue a modificar ahora el primer job y no se cambia ningún job previo.
- **POM:** la estructura actual ya provee Spring Batch y soporte de test; el diseño no requiere modificar dependencias ni el reactor Maven.

## 9. Archivos y estado final

Archivo creado:
- `ETAPA_8_DISENO_JOB_ESTADOS_ANUALES.md`

No se modificaron archivos existentes, POM, Java, CSV, salidas ni Semana 8. No se ejecutaron pruebas ni jobs en esta etapa documental. La implementación y ejecución quedan detenidas hasta que se confirmen el propósito del estado, período, agrupación, formatos de fecha, semántica de transacciones/signos, duplicados, descripción y política de errores.
