# Etapa 9 - Auditoría técnica e integración de arquitectura

**Fecha:** 2026-10-11  
**Alcance:** revisión estática y documental de Semana 9. No se modificaron código, POM, Compose, configuraciones, CSV ni salidas. No se ejecutaron pruebas, compilaciones, jobs, procesos batch ni contenedores.

## 1. Resumen ejecutivo

La estructura Maven está centralizada y coherente en versiones: Java 17, Spring Boot 3.1.6, Spring Cloud 2022.0.5, Resilience4j 2.0.2 y Spring Batch administrado por Boot. Esa coherencia estática no equivale a una compilación validada en esta etapa.

La plataforma aún es principalmente una base de módulos. Config Server y Eureka tienen sus servidores habilitados; el primer job de movimientos está implementado, tiene pruebas unitarias y cuenta con una ejecución exitosa documentada en etapas previas. No se encontró implementación funcional de APIs de Clientes, Cuentas, Pagos o BFF, ni consumidores/productores Kafka, políticas Resilience4j o seguridad efectiva. Los jobs de intereses y estados anuales están documentados, no implementados.

Los bloqueos más directos para despliegue local son: contraseña PostgreSQL vacía por defecto, ausencia del JAR de `batch-jobs` requerido por su Dockerfile, falta del CSV en la imagen/Compose y arranque automático del primer job al iniciar el contenedor batch. Compose tampoco incorpora Kafka ni Keycloak. No se validaron builds ni Compose dinámicamente por la restricción de esta etapa.

## 2. Versiones, módulos y dependencias

El POM raíz hereda de Spring Boot 3.1.6, fija `java.version` y `maven.compiler.release` en 17, importa el BOM Spring Cloud 2022.0.5 y administra Resilience4j 2.0.2. Los nueve POM de módulos heredan de ese parent mediante `../../pom.xml`; no se detectaron versiones de Boot/Cloud divergentes. Spring Batch se declara solo en `batch-jobs` y sus versiones las administra Spring Boot. Los drivers PostgreSQL están en runtime; H2 se usa para metadata batch local.

Los POM no declaran dependencias entre los módulos internos. Esto es compatible con servicios desplegables por separado, pero no hay contratos ni clientes que prueben comunicación funcional entre ellos. Kafka, seguridad OAuth2/JWT y Resilience4j aparecen como dependencias declaradas en servicios/BFF, no como flujos implementados.

| Módulo | Dependencias relevantes observadas | Evidencia de implementación actual |
|---|---|---|
| `platform/config-server` | Config Server, Actuator | `@EnableConfigServer`, perfil native y ubicación de repositorio configurada. El repositorio solo contiene configuración común mínima; no hay archivos por servicio. |
| `platform/eureka-server` | Eureka Server, Actuator | `@EnableEurekaServer`, registro/descubrimiento preparado. No prueba que exista comunicación de negocio. |
| `services/clientes` | Web, JPA, PostgreSQL, Config/Eureka, Kafka, OAuth2 Resource Server, Resilience4j | Clase de arranque y YAML. No se encontraron controller, entidad, repositorio, listener Kafka, productor ni filtro de seguridad. |
| `services/cuentas` | Mismas familias que Clientes | Clase de arranque y YAML; no hay implementación de dominio detectada. |
| `services/pagos` | Mismas familias que Clientes | Clase de arranque y YAML; no hay API, orquestación ni mensajería detectada. |
| `batch/batch-jobs` | Spring Batch, JDBC, PostgreSQL, H2, Config/Eureka, starter de test | Job diario de movimientos implementado. Intereses y estados anuales solo documentados en Etapas 7 y 8. |
| `bff/web`, `bff/mobile`, `bff/atm` | Web, Security, OAuth2 Client, Config/Eureka, Resilience4j | Clases de arranque y YAML; no se encontraron rutas, controladores, registro OAuth2 ni clientes a servicios. |

La configuración JPA usa `ddl-auto: validate`, pero no se encontraron entidades, repositorios ni migraciones de esquema (Flyway/Liquibase). Por ello no hay persistencia de dominio funcional que validar. Los servicios configuran `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; Batch usa H2 en memoria y no consume la base PostgreSQL de Compose para metadata.

## 3. Matriz de requisitos de evaluación

Estados: **Implementado** = comportamiento visible en código/configuración funcional; **Parcial** = piezas declaradas/configuradas sin flujo completo; **Documentado** = diseño o intención sin implementación; **Pendiente de evidencia** = requiere build, prueba o despliegue que no se ejecutó aquí.

| Requisito | Estado real | Evidencia disponible | Brecha / siguiente acción recomendada |
|---|---|---|---|
| Java 17 y versiones base | Parcialmente verificado | Parent POM fija Java 17, Boot 3.1.6 y Cloud 2022.0.5; Dockerfiles usan `eclipse-temurin:17-jre`. | Ejecutar validación de reactor y dependency convergence en una etapa autorizada; esta auditoría no compiló. |
| Microservicios de Clientes, Cuentas y Pagos | Parcial: módulos y clientes Eureka configurados, APIs no implementadas | Tres módulos, POMs web/JPA, YAML de puertos/config/discovery y clases `@SpringBootApplication`. | Definir e implementar entidades, contratos REST, validación, persistencia/migraciones y pruebas de contrato. |
| Config Server | Parcialmente implementado | `@EnableConfigServer`, perfil native, volumen de `platform/config-repo` en Compose. | Agregar y validar configuración por servicio/perfil; el repo actual contiene solo configuración común mínima. |
| Eureka | Parcialmente implementado | Servidor con `@EnableEurekaServer`; clientes incluyen starter y `defaultZone`. | Aportar evidencia de registro/descubrimiento con servicios operativos y health/readiness. |
| Comunicación asíncrona Kafka | Dependencia/configuración parcial; flujo no implementado | `spring-kafka` en tres servicios y `bootstrap-servers` configurable. Compose no contiene broker; variable vacía en `.env.example`; no se hallaron listeners, templates ni productores. | Definir broker/entornos, tópicos, contratos, productores/consumidores, retry/DLQ y pruebas de integración. |
| Resiliencia con Resilience4j | Dependencia solamente | Versión 2.0.2 administrada y dependencias en servicios/BFF. No se encontraron anotaciones ni propiedades de circuit breaker/retry/bulkhead. | Definir llamadas protegidas, umbrales, fallback y tests de comportamiento; una dependencia no prueba resiliencia. |
| Seguridad | Parcial a nivel de dependencias, no funcional | Resource Server en servicios, OAuth2 Client/Security en BFF y variable `KEYCLOAK_ISSUER_URI`. No existe configuración issuer/client, SecurityFilterChain ni servicio Keycloak en Compose. | Aprobar proveedor/issuer, clientes, roles y secretos; configurar JWT/OAuth2 y verificar endpoints protegidos. |
| Spring Batch | Primer job implementado; segundo y tercero documentados | `movimientosReporteDiarioJob`, reader/processor/writer/listener y 2 clases JUnit del módulo. Etapas 7 y 8 describen intereses y estados, sin código. | El primer job no se volvió a ejecutar por instrucción; la ejecución previa está documentada, pero falta evidencia de integración posterior a los cambios de Etapa 6.1. Implementar los otros solo con autorización/reglas. |
| Pruebas automatizadas | Cobertura limitada al módulo Batch; evidencia parcial | `src/test` solo contiene `MovimientoDiarioValidatorTest` y `MovimientoBatchCountersTest`. Una ejecución previa registrada dio 10 tests aprobados; no se repitió aquí. | Agregar tests de servicios, seguridad, mensajería, configuración y Compose; ejecutar la suite en una etapa autorizada. |
| Docker | Parcial; Compose presenta bloqueos verificables estáticamente | Nueve Dockerfiles JRE 17 copian JARs. El resto de módulos tiene JAR actual en `target`; no se encontró `batch-jobs/target/batch-jobs-1.0.0-SNAPSHOT.jar`, que su Dockerfile intenta copiar. | Construir artefactos antes de Compose o definir build reproducible; validar imágenes sin afectar salidas batch en etapa autorizada. |
| Persistencia | PostgreSQL parcial; dominio no implementado | Servicio PostgreSQL 16 con volumen nombrado y healthcheck; tres servicios configuran datasource. | Definir esquema y migraciones; revisar credenciales no vacías; decidir persistencia durable de metadata Batch y de resultados. |
| Despliegue en nube | Documentado como pendiente, no implementado | README declara que no hay despliegue AWS. No se encontraron manifiestos Kubernetes, Terraform/Bicep, workflows CI/CD o despliegue cloud. | Definir destino y arquitectura, secretos, redes, observabilidad, pipeline e IaC; presentar evidencia de despliegue. |

## 4. Revisión de Docker Compose y contenedores

### Bloqueos de prioridad alta

1. **Contraseña PostgreSQL por defecto vacía.** `docker-compose.yml` usa `${POSTGRES_PASSWORD:-}` y `.env.example` también la deja vacía. La imagen oficial de PostgreSQL requiere contraseña no vacía salvo configuración explícita de modo trust, que no existe. Con valores de ejemplo/default, el servicio de base de datos no puede inicializarse correctamente.
2. **JAR de Batch ausente.** `batch/batch-jobs/Dockerfile` hace `COPY batch/batch-jobs/target/batch-jobs-1.0.0-SNAPSHOT.jar`, pero esa ruta no existe en el estado inspeccionado. Los Dockerfiles no tienen etapa Maven; Compose no genera el JAR por sí mismo. El build de esa imagen queda bloqueado hasta empaquetar el módulo.
3. **Batch incluido ejecuta el job de movimientos al iniciar.** `BatchJobsApplication` tiene un `ApplicationRunner` que lanza explícitamente `movimientosReporteDiarioJob`. La imagen copia solo el JAR y Compose no monta `data/semana_3`, ni configura `MOVIMIENTOS_INPUT_PATH`; el CSV por defecto no estaría presente dentro del contenedor. Si el JAR se construye y se inicia ese servicio, el reader no podrá abrir su entrada. No se inició el contenedor ni el job durante esta auditoría.
4. **Kafka y Keycloak ausentes.** Compose no define brokers/IdP y las variables correspondientes quedan vacías en `.env.example`. No hay configuración para suplir esas dependencias ni implementación consumidora/productora o OAuth que permita validar las funciones declaradas.

### Otros riesgos de integración

- `depends_on` enumera PostgreSQL, Config Server y Eureka, pero no usa condiciones `service_healthy`; solo PostgreSQL tiene healthcheck. Hay riesgo de carreras de arranque, y Config/Eureka no tienen healthchecks declarados.
- La contraseña de base de datos por defecto vacía también se propaga a los servicios. No hay gestión de secretos para despliegue; `.env.example` es una plantilla, no un secreto válido.
- Solo PostgreSQL tiene volumen durable. Metadata Batch está en `jdbc:h2:mem:batchdb`; salidas batch no tienen volumen y serían efímeras dentro del contenedor.
- `batch-jobs` declara `depends_on: postgres` y recibe `DB_URL`, pero su YAML configura H2 en memoria y no consume esa conexión PostgreSQL; la dependencia actual es inconsistente/superflua para el job implementado.
- El CSV requerido por el primer job no se incorpora a la imagen ni se monta con volumen; el directorio de salida tampoco está persistido.
- Los servicios Clientes/Cuentas/Pagos y Batch usan `expose` interno, no publicación host; BFF, Config Server, Eureka y PostgreSQL sí publican puertos. Esto puede ser deliberado, pero depende de que existan rutas BFF, que aún no están implementadas.
- Los Dockerfiles no declaran usuario no privilegiado ni healthcheck. Compose tampoco define restart policy ni límites de recursos. Actuator expone `health,info`, pero esos endpoints no se usan como readiness en Compose.
- Las imágenes esperan que los JAR tengan nombres/versiones exactos; no hay build multi-stage ni pipeline que garantice que los artefactos existan antes de `docker compose build`.
- No se validó `docker compose config` ni se levantaron contenedores, conforme a la instrucción.

## 5. Contradicciones y continuidad documental (Etapas 5–8)

- **Etapa 5 frente a Etapa 8:** Etapa 5 informa 197 fechas ambiguas para `estados_financieros_anuales.csv`; Etapa 8 reporta 179 usando como criterio que D/M/A y M/D/A sean ambas fechas válidas y distintas. Falta el script o procedimiento exacto de Etapa 5, el criterio aplicado y la lista de filas que forman la diferencia de 18. Sin esos elementos no se puede determinar la causa ni elegir un conteo oficial. No se alteran esos documentos ni el CSV.
- **README frente a Etapa 6:** README dice que los módulos solo contienen POM, clase de arranque y configuración y que no hay jobs configurados. La implementación y pruebas del primer job de movimientos contradicen esa descripción; README requiere actualización cuando se autorice edición documental.
- **Etapa 4 frente a Etapa 5:** Etapa 4 documenta que aún no había CSV locales; Etapa 5 confirma su disponibilidad y análisis. Es consistente como secuencia histórica, pero Etapa 4 ya no representa el estado actual.
- **Etapas 7 y 8:** ambas mantienen los jobs de intereses y estados anuales en diseño, sin fórmula/agregación implementadas; esto es coherente con las reglas de negocio pendientes.
- **Falta de Etapa 6/6.1 documental:** no hay archivo `ETAPA_6*.md` o `ETAPA_6_1*.md` en la raíz, aunque el código del job y sus pruebas existen. La ejecución y los resultados se registraron en la conversación, no en un informe de etapa del repositorio.

## 6. Prioridad de brechas y acciones sugeridas

| Prioridad | Brecha | Siguiente acción (requiere autorización) |
|---|---|---|
| P1 | Compose no inicia con contraseña PostgreSQL vacía por defecto. | Definir configuración local segura y obligatoria; no usar trust en entornos reales. |
| P1 | Imagen de Batch no puede copiar el JAR ausente; no hay build multi-stage. | Establecer build reproducible y comprobar artefacto antes de construir la imagen. |
| P1 | Al iniciar Batch en Compose se dispara el primer job sin el CSV montado. | Separar lanzamiento de jobs del arranque del servicio y diseñar input/output explícitos/persistentes; proteger los artefactos actuales. |
| P1 | Kafka y seguridad/Keycloak no están disponibles ni conectados. | Confirmar proveedores y contratos, configurar dependencias y aportar tests de integración/autenticación. |
| P1 | No existen APIs, persistencia de dominio ni contratos entre servicios/BFF. | Definir requisitos/contratos antes de implementar; probar llamadas y datos end-to-end. |
| P2 | Sin readiness/healthchecks coordinados, restart policy o pruebas de Compose. | Añadir healthchecks y dependencias saludables; validar perfiles y recuperación en entorno controlado. |
| P2 | Metadata Batch en H2 en memoria y outputs efímeros en contenedor. | Decidir persistencia de metadata/resultados y estrategia de retención/idempotencia. |
| P2 | Sin evidencia de build, tests completos de reactor o despliegue cloud en esta auditoría. | Ejecutar gates autorizados y documentar evidencia; definir primero el destino cloud. |
| P2 | Documentación de alcance/fechas desactualizada o no reconciliada. | Actualizar README y anexar metodología/filas para resolver 179 vs 197. |

## 7. Límites de verificación y archivos

Se inspeccionaron los diez POM, `docker-compose.yml`, los nueve Dockerfiles, YAML de módulos y repositorio de configuración, clases de arranque y fuentes Java, `.env.example`, README y documentos de Etapas 5 a 8. La inspección de fuentes no encontró controladores, entidades, repositorios, listeners/productores Kafka, filtros de seguridad ni políticas Resilience4j. Solo se encontraron dos clases de tests, ambas en Batch.

No se ejecutó Maven, pruebas, Docker Compose, contenedores, jobs ni procesos batch. El estado de compilación actual queda **pendiente de evidencia**; las brechas de Docker/Compose descritas arriba provienen de rutas y valores estáticos inspeccionados.

Archivo creado: `ETAPA_9_AUDITORIA_ARQUITECTURA.md`. No se modificó ningún otro archivo.
