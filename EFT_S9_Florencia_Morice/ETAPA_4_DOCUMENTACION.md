# Etapa 4 - Análisis técnico y preparación de los datos de entrada

## 1. Estado real de la localización de los CSV

Se verificó la estructura actual del proyecto Semana 9 y no existen archivos CSV ni una carpeta `data` dentro del repositorio local.

Evidencia obtenida en esta etapa:
- no se encontró ningún archivo con extensión `.csv` bajo la carpeta del proyecto;
- no existe un directorio `data` ni `data/semana_3` dentro del proyecto actual;
- la única referencia local válida de los archivos es la mencionada en [README.md](README.md), que indica la ubicación origen: https://github.com/KariVillagran/fin_legacy_data.

Por lo tanto, los tres archivos de referencia deben obtenerse desde ese repositorio y, en la práctica, desde la carpeta esperada `data/semana_3` del proyecto de origen. No se inventan nombres ni contenidos adicionales.

### Archivos esperados según la referencia del proyecto

- movimientos financieros diarios
- intereses trimestrales
- estados financieros anuales

La referencia del README identifica los encabezados esperados, pero no se pudo acceder a los archivos dentro del proyecto ni comprobar filas reales. Lo que sí es verificable es el siguiente punto documental:

- Movimientos: `id, fecha, monto, tipo`
- Intereses: `cuenta_id, nombre, saldo, edad, tipo`
- Estados anuales: `cuenta_id, fecha, transaccion, monto, descripcion`

## 2. Análisis de encabezados y calidad de datos esperada

### 2.1 Resumen de la evidencia disponible

El README indica que los archivos legacy presentan problemas de calidad antes de cualquier cálculo financiero. Estos problemas se describen como riesgos de entrada y no como hechos verificados sobre registros reales dentro del proyecto actual.

Problemas reportados:
- fechas en distintos formatos;
- campos vacíos;
- montos inválidos;
- tipos desconocidos;
- duplicados;
- valores fuera de rango;
- archivo de intereses trimestral sin especificar tasa ni fórmula mensual;
- archivo de movimientos sin identificación clara de la cuenta asociada.

### 2.2 Evaluación por tipo de archivo

#### a) Movimientos financieros diarios
Encabezado documentado: `id, fecha, monto, tipo`

Riesgos esperables:
- `id` vacío o duplicado;
- `fecha` con formato inválido o no estándar;
- `monto` vacío, cero, negativo o con formato no numérico;
- `tipo` no reconocido o faltante;
- registros repetidos o inconsistentes en el mismo día.

No se puede afirmar que estos problemas ocurran en registros concretos porque no se encontró el archivo local ni se revisaron filas reales.

#### b) Intereses trimestrales
Encabezado documentado: `cuenta_id, nombre, saldo, edad, tipo`

Riesgos esperables:
- `cuenta_id` faltante o no correlacionado;
- `nombre` vacío o no alineado con la entidad real del cliente;
- `saldo` inválido, vacío o con formato incorrecto;
- `edad` fuera de rango o no numérica;
- `tipo` no reconocido;
- filas duplicadas por cuenta y periodo;
- ausencia explícita de tasa, fórmula y regla de cálculo con la que se construye el interés trimestral.

La ausencia de fórmula es una condición de negocio que debe confirmarse antes de cualquier cálculo financiero. No se asume ni se inventa ninguna política.

#### c) Estados financieros anuales
Encabezado documentado: `cuenta_id, fecha, transaccion, monto, descripcion`

Riesgos esperables:
- `cuenta_id` nula o no asociada a una cuenta válida;
- `fecha` con múltiples formatos o fuera de rango;
- `transaccion` con valores no esperados;
- `monto` vacío, cero o inválido;
- `descripcion` vacía o sin clasificar;
- duplicados por combinación de cuenta, fecha y transacción.

## 3. Reglas de validación propuestas

### 3.1 Errores que sí pueden detectarse técnicamente

Estos son errores que pueden validarse con reglas de lectura, parsers y validaciones de dominio sin requerir interpretación financiera avanzada.

- campo obligatorio faltante;
- valor nulo o vacío;
- formato de fecha no soportado;
- formato de monto no numérico;
- monto menor o igual a cero si la regla de negocio así lo exige;
- tipo no reconocido entre los permitidos;
- registro duplicado por clave natural o compuesto;
- longitud o rango fuera de límites esperados;
- archivo con cabecera incorrecta o columnas desordenadas;
- valores fuera de rango del tipo de dato esperado.

Estas reglas son estrictamente técnicas y deben implementarse en la capa de carga del batch.

### 3.2 Decisiones que debe confirmar el negocio

Estas decisiones no pueden resolverse solo con la inspección técnica del archivo. Deben ser aprobadas por el negocio antes de usar los datos en cálculos o en la migración final.

- cuál es la regla exacta para identificar la cuenta asociada a cada movimiento;
- cuál es la definición de saldo válido, si existe o si se deriva del histórico;
- si el monto cero debe rechazarse siempre o aceptarse solo en determinados casos;
- si los registros con montos negativos están permitidos solo para ciertos tipos de movimiento;
- cuál es la regla de clasificación para los tipos de transacción y de interés;
- qué se hace con registros duplicados: descartarlos, consolidarlos o requerir revisión humana;
- si el archivo de intereses trimestrales representa un dato de historial o una regla de cálculo a aplicar; 
- cómo se define la fórmula o tasa de interés, si esta existe; 
- si la fecha de referencia se interpreta como fecha de operación, fecha de cierre o fecha de registro;
- cuál es la política para campos vacíos o valores anómalos en la fuente legacy.

No se deben inventar tasas, fórmulas de interés, saldos iniciales ni políticas contables. Toda esta lógica debe confirmarse y documentarse antes de implementar la lógica financiera.

## 4. Cómo registrar registros válidos y rechazados

La estrategia recomendada para Spring Batch es separar la ingesta en dos flujos claramente auditable:

### 4.1 Registro de válidos
Los registros que pasan la validación técnica deben almacenarse en una etapa intermedia, con metadata mínima que permita trazabilidad.

Campos mínimos sugeridos:
- origen del archivo;
- nombre del dataset;
- número de línea;
- hash interno o identificador temporal;
- fecha de carga;
- estado del registro;
- valores normalizados.

Este flujo debe permitir que más adelante el batch los procese de manera ordenada, idempotente y auditada.

### 4.2 Registro de rechazados
Los registros fallidos deben registrarse en un archivo o tabla de errores con una causa específica.

Estructura sugerida:
- línea original;
- motivo de rechazo;
- detalle técnico del error;
- tipo de validación fallida;
- archivo de origen;
- fecha de procesamiento;
- estado: rechazado, pendiente, revisión manual.

Esto permite:
- distinguir error técnico de decisión de negocio;
- auditar el proceso sin perder información;
- preparar futuras reintentos o revisiones humanas.

### 4.3 Recomendación de diseño de auditoría

Se recomienda usar una salida estándar de lote con dos resultados:
- registros válidos para procesamiento;
- registros rechazados con motivos de rechazo.

Esto garantiza que los jobs de Spring Batch puedan ejecutarse con trazabilidad, idempotencia y evidencia de calidad de entrada.

## 5. Ejemplos de pruebas para datos válidos e inválidos

Estas pruebas son ejemplos de validación de entrada y no implementan la lógica financiera ni la ejecución real de batch.

### 5.1 Datos válidos

Ejemplo conceptual para movimientos:
- id: 1001
- fecha: 2024-03-15
- monto: 15000.50
- tipo: ingreso

Ejemplo conceptual para intereses:
- cuenta_id: C-001
- nombre: Cliente A
- saldo: 150000.00
- edad: 35
- tipo: ahorro

Ejemplo conceptual para estados anuales:
- cuenta_id: C-001
- fecha: 2024-12-31
- transaccion: cierre
- monto: 25000.00
- descripcion: cierre anual

Estos ejemplos cumplen la estructura esperada descrita en el README, sin introducir reglas financieras que aún no han sido confirmadas.

### 5.2 Datos inválidos

Ejemplo de formato inválido:
- id: vacío
- fecha: 15/03/2024
- monto: ABC
- tipo: desconocido

Ejemplo de fecha fuera de regla:
- fecha: 2024-99-99

Ejemplo de valor fuera de rango:
- saldo: -5000
- edad: 999
- tipo: no_existe

Ejemplo de registro duplicado:
- misma combinación de cuenta_id + fecha + transaccion repetida en el mismo archivo

Ejemplo de fila incompleta:
- cuenta_id: C-002
- nombre: vacío
- saldo: vacío

Estas pruebas deben estar documentadas como casos de validación técnica y no como prueba de cálculo financiero final.

## 6. Plan de preparación para Spring Batch

Se recomienda preparar la migración en estas fases, sin implementar todavía lógica financiera ni jobs productivos:

1. carga física de los tres archivos desde el repositorio legado;
2. validación técnica por fila y columna;
3. separación en registros válidos y rechazados;
4. normalización de nombres de columnas y tipos de dato;
5. registro de errores con trazabilidad y auditoría;
6. preparación de la capa siguiente para dominio y cálculo de negocio.

## 7. Conclusión de la Etapa 4

La evidencia disponible confirma que no existen los CSV dentro del proyecto Semana 9, por lo que no se puede revisar su contenido real ni afirmar detalladamente filas concretas sin obtenerlos del repositorio de origen. La base técnica que sí es verificable en este repositorio es la referencia documental del README y la estructura del proyecto.

La Etapa 4 queda preparada para continuar con la siguiente fase, pero solo después de confirmar la obtención de los archivos fuente y la validación del negocio sobre reglas de entrada y calidad de datos. No se implementan cálculos financieros ni jobs aún.

## 8. Archivos modificados

- [ETAPA_4_DOCUMENTACION.md](ETAPA_4_DOCUMENTACION.md)

## 9. Resultado concreto del análisis

- Los tres CSV no están disponibles localmente en este proyecto.
- El único origen documentado es la referencia oficial del README: https://github.com/KariVillagran/fin_legacy_data.
- Los encabezados documentados y verificables son los indicados arriba.
- Los problemas de calidad se registran como riesgos reportados por la documentación del proyecto, no como hallazgos comprobados sobre filas reales en este repositorio.
- Las reglas de validación técnica se separan claramente de las decisiones de negocio pendientes.
- Se propone una estrategia de validación, auditoría y separación de registros válidos/rechazados para prepararse para Spring Batch.

## 10. Siguiente paso

La documentación de la Etapa 4 está lista. Detengo la ejecución aquí y espero tu autorización antes de la siguiente etapa.
