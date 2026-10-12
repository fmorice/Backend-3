# Etapa 11 - Validación Docker de Spring Batch

**Fecha:** 2026-10-11  
**Alcance:** imagen y ejecución aislada del servicio `batch-jobs` para `movimientosReporteDiarioJob`. No se validó Compose completo ni se ejecutaron otros servicios.

## 1. Configuración revisada y ajuste autorizado

El servicio Compose es `batch-jobs`, imagen `eft-s9/batch-jobs:local`, bajo el perfil `batch`. No declara `depends_on`. El Dockerfile construye el JAR con `mvn -pl batch/batch-jobs -am package` en Maven/Temurin 17 y lo copia a `eclipse-temurin:17-jre`. `.dockerignore` excluye `.env`, `target`, los CSV y salidas históricas del contexto de build.

Batch requiere `BATCH_DB_PASSWORD` no vacía. Su datasource está configurado con `jdbc:h2:mem:batchdb`, así que la metadata Spring Batch fue temporal/en memoria; PostgreSQL no era necesario para esta ejecución.

Se cambió únicamente `docker-compose.yml`, con autorización, para:
- configurar `MOVIMIENTOS_OUTPUT_DIR=/app/output`;
- montar `batch/batch-jobs/output/validation/docker/${BATCH_RUN_ID}` directamente en `/app/output`.

La resolución efectiva para `BATCH_RUN_ID=etapa11-run1` confirmó este bind mount exacto:

`/Users/florenciamorice/Documents/GitHub/Backend-3/EFT_S9_Florencia_Morice/batch/batch-jobs/output/validation/docker/etapa11-run1 -> /app/output` (escritura)

El CSV se montó desde `data/semana_3` hacia `/app/input` en solo lectura. La salida anterior de Etapa 10 y las salidas históricas no forman parte de este mount.

## 2. Construcción de imagen

Docker respondió con cliente y daemon disponibles: Docker 28.5.1, Docker Desktop 4.48.0, Linux/arm64.

Comando ejecutado (las contraseñas se generaron aleatoriamente y sus valores no se imprimieron ni guardaron):

```text
export POSTGRES_PASSWORD="$(openssl rand -hex 24)" && export BATCH_DB_PASSWORD="$(openssl rand -hex 24)" && export BATCH_RUN_ID=etapa11-run1 && docker compose --profile batch build batch-jobs
```

Resultado: **build finalizado correctamente**; se etiquetó `eft-s9/batch-jobs:local`. La construcción ejecutó el paso Maven `-pl batch/batch-jobs -am package` dentro de la etapa de build. No inició la aplicación ni ejecutó el job durante la construcción.

## 3. Verificación previa y ejecución

Antes de ejecutar:
- Compose efectivo mostró `MOVIMIENTOS_INPUT_PATH=/app/input/movimientos_financieros_diarios.csv` y `MOVIMIENTOS_OUTPUT_DIR=/app/output`.
- `BATCH_DB_PASSWORD` se resolvió como no vacía; el valor no se mostró.
- `batch-jobs` apareció sin dependencias (`depends_on={}`) y bajo perfil `batch`.
- `batch/batch-jobs/output/validation/docker/etapa11-run1` no existía.
- El CSV fuente era legible.
- Se registraron hashes de los CSV originales, salidas históricas y salidas de Etapa 10 antes de ejecutar.

Comando ejecutado una sola vez:

```text
export POSTGRES_PASSWORD="$(openssl rand -hex 24)" && export BATCH_DB_PASSWORD="$(openssl rand -hex 24)" && export BATCH_RUN_ID=etapa11-run1 && docker compose --profile batch run --rm --no-deps --pull never batch-jobs
```

El uso de `--no-deps` evitó iniciar servicios dependientes; no se inició PostgreSQL, Kafka, Keycloak, Eureka ni los microservicios. Compose creó la red del proyecto para el contenedor efímero. El contenedor se ejecutó con Java 17.0.18 y utilizó H2 en memoria, según los logs.

**Resultado:** `movimientosReporteDiarioJob` terminó con estado **COMPLETED** en 933 ms. El comando de ejecución finalizó correctamente y `--rm` retiró el contenedor al terminar.

## 4. Resultados, archivos y conciliación

Se generaron exactamente estos archivos en `batch/batch-jobs/output/validation/docker/etapa11-run1/`:

- [CSV válidos](batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_validos.csv)
- [CSV rechazados](batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_rechazados.csv)
- [Resumen](batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_resumen.txt)

Conteos del resumen y confirmación independiente mediante lectura CSV:

| Métrica | Resultado |
|---|---:|
| Registros leídos | 1.000 |
| Escritos después de validación técnica | 391 |
| Pendientes de decisión de negocio | 71 |
| Validados técnicamente sin decisión de negocio | 320 |
| Aceptados por reglas de negocio confirmadas | 0 |
| Rechazados | 609 |
| Errores de lectura | 0 |
| Errores de escritura | 0 |
| Conciliación | `true` |

La conciliación primaria es `1.000 = 391 + 609 + 0 + 0`. Los 71 pendientes pertenecen a los 391 escritos: `391 = 71 + 320 + 0 aceptados`. No se suman como categoría primaria adicional ni se interpretan como aceptación financiera.

Los CSV tienen 392 y 610 líneas físicas (incluyen encabezado), respectivamente. El parser confirmó 391 filas válidas —71 `PENDIENTE_NEGOCIO` y 320 `VALIDADO_TECNICAMENTE`— y 609 rechazadas.

## 5. Integridad de fuentes y artefactos previos

Los SHA-256 medidos antes y después de la ejecución coinciden:

| Archivo | SHA-256 antes/después |
|---|---|
| `data/semana_3/estados_financieros_anuales.csv` | `cf62d3ad676cb739dfc5ccae26612f3a107ebf7560c31b7ab2f1044592aae7e0` |
| `data/semana_3/intereses_trimestrales.csv` | `c6aa08454600d1c355d6306d8d8941a1e9c0a1d2f6c275177aa0735db255d23a` |
| `data/semana_3/movimientos_financieros_diarios.csv` | `f531fa330f1dc0e0b3511ea8b701dc62a63d98f0a92304b99f8e2ca6c29ef4b8` |
| Salida histórica: `movimientos_diarios_validos.csv` | `865643329221c157b25983e4820b185e7e50e47143b5969a72203c24b8aaa3cf` |
| Salida histórica: `movimientos_diarios_rechazados.csv` | `0eb0221c5da88583d7d5e911636c233984fcae53388671ff69bb2f6c57ed54b8` |
| Salida histórica: `movimientos_diarios_resumen.txt` | `15caa59406025178e8bf89cec62100ab33c21d286c32898aa2e981d71e7fb08a` |
| Etapa 10: `movimientos_diarios_validos.csv` | `7a1b80e37cb2a0c00ed0c23fc82e5059caf53916bcf21e34ab314f1c909cde8e` |
| Etapa 10: `movimientos_diarios_rechazados.csv` | `9c87a1873c237663477a0752e1125ccfe12515d6d24de9b975f443c5b676193c` |
| Etapa 10: `movimientos_diarios_resumen.txt` | `69e90655428df1f690d4e36acc7e8b15af333ff45a89a6b4d58bc22d69aaa372` |

Todos los CSV originales se montaron como solo lectura. La salida se montó exclusivamente desde el directorio nuevo de Etapa 11.

## 6. Alcance y limitaciones

- Se validaron la construcción de la imagen Batch y la ejecución real de un contenedor Batch aislado; **no se validó la arquitectura completa de Docker Compose**.
- No se iniciaron dependencias ni servicios externos. Kafka, Keycloak, Eureka, microservicios, BFF, seguridad y despliegue cloud no fueron probados.
- PostgreSQL no participó. La metadata Spring Batch vive en H2 en memoria y no persiste al finalizar el proceso.
- El contenedor se ejecutó como `root`, visible en el log. El endurecimiento de usuario/imagen queda pendiente y fuera de esta etapa.
- Permanecen pendientes las decisiones de negocio del job: 71 registros siguen pendientes; no se aprobaron tasas, fórmulas ni reglas financieras.

## 7. Archivos modificados y creados

Modificado:
- `docker-compose.yml` (solo aislamiento de la salida de Batch autorizado para Etapa 11).

Creados:
- `batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_validos.csv`
- `batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_rechazados.csv`
- `batch/batch-jobs/output/validation/docker/etapa11-run1/movimientos_diarios_resumen.txt`
- `ETAPA_11_VALIDACION_DOCKER_BATCH.md`

No se modificaron los CSV originales, Semana 8, código Java ni reglas del job. No se avanzó a Etapa 12.
