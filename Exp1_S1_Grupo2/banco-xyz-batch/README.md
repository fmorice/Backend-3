# Banco XYZ - Batch (Semana 1)

Proyecto de ejemplo simple para aprender Spring Batch: lectura de CSV → procesamiento → escritura en H2.

Cómo ejecutar

1. Desde la raíz del repositorio ejecuta:

```bash
cd Semana1-Fundamentos-RutaExpress/banco-xyz-batch
mvn spring-boot:run
```

2. Alternativa (generar jar):

```bash
mvn package
java -jar target/banco-xyz-batch-0.1.0.jar
```

Qué hace el Job 1

- Job: `reporteTransaccionesJob` (se ejecuta automáticamente al iniciar).
- Step: `reporteTransaccionesStep` (lee en chunks de 5 registros).
- ItemReader: lee `src/main/resources/data/transacciones.csv` y crea objetos `Transaccion`.
- ItemProcessor: `TransaccionProcessor` filtra transacciones con `monto == 0` (no se escriben).
- ItemWriter: inserta registros válidos en la tabla H2 `transaccion_procesada`.

Comprobar resultados en H2

1. Abrir consola H2 en el navegador: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:bankdb`
   - Usuario: `sa` (sin contraseña)

2. Ejecutar la consulta:

```sql
SELECT * FROM transaccion_procesada;
```

Ver logs

En la salida de la aplicación verás un resumen del Step con contadores:
- `readCount` = registros leídos
- `writeCount` = registros escritos
- `filterCount` = registros filtrados (monto == 0)

Job 2: calculoInteresesJob (breve)

- Job: `calculoInteresesJob` (se ejecuta automáticamente al iniciar junto al Job 1).
- Lee `src/main/resources/data/intereses.csv`, calcula un `tasa` simple según el `tipo` y `interes = saldo * tasa`.
- Filtra registros con `saldo <= 0`.
- Guarda los resultados en la tabla H2 `interes_procesado`.

Archivos importantes

- `src/main/resources/data/transacciones.csv` — CSV de entrada
- `src/main/resources/schema.sql` — crea la tabla `transaccion_procesada`
- `src/main/java/cl/duoc/bancoxyz/batch/config/BatchConfig.java` — Job / Step / Reader / Writer
- `src/main/java/cl/duoc/bancoxyz/batch/processor/TransaccionProcessor.java` — Processor

Acceso rápido a H2 Console

- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:bankdb
- Usuario: sa
- Contraseña: (vacía)
