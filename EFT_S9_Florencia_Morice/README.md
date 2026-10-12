# EFT Backend III - Semana 9 - Banco XYZ

Proyecto nuevo e independiente para la Evaluación Final Transversal. Esta carpeta no reutiliza enlaces simbólicos ni modifica el proyecto de Semana 8.

## Base tecnológica

- Java 17
- Spring Boot 3.1.6 y Spring Cloud 2022.0.5, alineados con las versiones revisadas en Semana 8
- Spring Batch administrado por el BOM de Spring Boot
- Resilience4j 2.0.2

Estas versiones permiten mantener compatibilidad con la base existente; antes de un despliegue real se debe revisar su ciclo de soporte y seguridad.

## Módulos

- `platform/config-server`: configuración centralizada.
- `platform/eureka-server`: descubrimiento de servicios.
- `services/clientes`, `services/cuentas`, `services/pagos`: esqueletos de servicios de dominio.
- `batch/batch-jobs`: espacio para tres jobs Spring Batch independientes.
- `bff/web`, `bff/mobile`, `bff/atm`: esqueletos de BFF por canal.

Los módulos de servicios y BFF contienen principalmente clases de arranque y configuración base. `batch/batch-jobs` incluye el job diario `movimientosReporteDiarioJob`; los jobs de intereses y estados anuales permanecen en fase de diseño. No hay endpoints funcionales ni persistencia de dominio implementados, y las reglas financieras siguen pendientes de confirmación.

## Datos y reglas pendientes

Referencia de datos: <https://github.com/KariVillagran/fin_legacy_data>.
Los encabezados revisados en `data/semana_3` son:

- Movimientos: `id, fecha, monto, tipo`.
- Intereses: `cuenta_id, nombre, saldo, edad, tipo`.
- Estados anuales: `cuenta_id, fecha, transaccion, monto, descripcion`.

Hay fechas en formatos distintos, campos vacíos, montos inválidos, tipos desconocidos, duplicados y valores fuera de rango. El archivo de intereses es trimestral y no especifica tasa ni fórmula mensual. El archivo de movimientos tampoco identifica claramente una cuenta. No se calcularán intereses ni saldos hasta confirmar esas reglas.

## Configuración local

1. Configurar Java 17 en la terminal.
2. Copiar `.env.example` a `.env` y completar solo valores locales requeridos. `.env` está ignorado por Git; no guardar secretos en este README.
3. Empaquetar los módulos: `mvn -DskipTests package`.
4. Revisar la configuración: `docker compose config`.
5. Iniciar solo después de configurar base de datos, Keycloak y conectividad con Kafka.

Compose es una plantilla de infraestructura, no evidencia de que los servicios ya arranquen o se comuniquen. Kafka y Keycloak quedan externos hasta definir sus versiones y direcciones reales. No hay despliegue AWS realizado.

## Etapas siguientes

1. Confirmar reglas de negocio y archivos de entrada/salida.
2. Implementar persistencia, validaciones e idempotencia en los servicios.
3. Implementar y probar los tres jobs con reinicio seguro y errores selectivos.
4. Definir contratos, seguridad y comunicación de cada BFF.
5. Completar Compose, configuración EC2 y pruebas de extremo a extremo.
6. Actualizar instrucciones, despliegue, informe según plantilla oficial y evidencias finales.
7. Preparar el guion de video de 5 a 7 minutos.
