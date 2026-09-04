# Migración Batch Bancaria

Aplicación Spring Boot con Spring Batch para procesar archivos CSV de operaciones bancarias y generar reportes consolidados en base de datos relacional.

## Qué hace este proyecto

El sistema incluye tres jobs independientes:

1. `transaccionJob`  
   Lee `transacciones.csv`, valida cada registro y guarda las transacciones en la base de datos. Cada chunk usa tamaño `5` y se ejecuta con un `ThreadPoolTaskExecutor` de 3 hilos.

2. `interesJob`  
   Procesa los datos de interés por cuenta y calcula el saldo final aplicando tasas según el tipo de producto (`ahorro`, `prestamo`, `hipoteca`, etc.).

3. `cuentaAnualJob`  
   Consolida información anual por cuenta, calculando montos, depósitos, retiros, total de movimientos y saldo anual.

## Stack tecnológico

- Java 21
- Spring Boot 3.4.3
- Spring Batch
- Spring Data JPA
- Maven Wrapper
- Base de datos: MySQL compatible por configuración; por defecto usa H2 para pruebas locales

## Configuración de base de datos

El proyecto está preparado para usar MySQL en un entorno real, pero conserva H2 como valor por defecto para desarrollo y pruebas locales.

Variables soportadas:

```bash
DB_URL=jdbc:mysql://localhost:3306/bankdb?createDatabaseIfNotExist=true
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_password
DB_DIALECT=org.hibernate.dialect.MySQLDialect
```

Si no se configuran, la aplicación usa:

```properties
jdbc:h2:mem:bankdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

## Estructura principal

```text
src/
├── main/
│   ├── java/com/duoc/migracion/
│   │   ├── config/          # Configuración de jobs y executor
│   │   ├── controller/      # Endpoints para disparar jobs
│   │   ├── dto/             # DTOs de lectura desde CSV
│   │   ├── exception/       # Excepciones específicas de validación y fallos
│   │   ├── listener/        # Listeners de job y skips
│   │   ├── model/           # Entidades JPA
│   │   ├── processor/       # Lógica de validación y transformación
│   │   ├── reader/          # Lectores FlatFileItemReader
│   │   ├── repository/      # Repositorios Spring Data JPA
│   │   ├── writer/          # Escritores de datos
│   │   ├── policy/          # Política de tolerancia a fallos
│   │   └── MigracionBatchApplication.java
│   └── resources/
│       ├── application.properties
│       └── data/
│           ├── transacciones.csv
│           ├── intereses.csv
│           └── cuentas_anuales.csv
└── test/java/com/duoc/migracion/
    └── MigracionBatchApplicationTests.java
```

## Reglas implementadas en batch

- `chunk(5, transactionManager)` en cada step.
- `ThreadPoolTaskExecutor` configurado con 3 threads, con prefijo `batch-thread-`.
- Policies de tolerancia a fallos con `faultTolerant()`.
- `skipLimit` para omitir registros inválidos sin abortar el proceso.
- Validación de fechas, montos, tipos y registros incorrectos.
- Uso de `BigDecimal` para valores monetarios.

## Endpoints para ejecutar jobs

La aplicación expone endpoints REST para lanzar los jobs manualmente:

```text
GET /api/batch/run/transaccion
GET /api/batch/run/interes
GET /api/batch/run/cuenta-anual
```

## Cómo ejecutar

### Ejecutar pruebas

```bash
./mvnw clean test
```

### Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

Luego se pueden invocar los endpoints anteriores desde el navegador o con `curl`.

## Validación

La suite actual queda en verde con Maven:

```bash
./mvnw clean test
```

Resultado esperado: `BUILD SUCCESS` y `EXIT:0`.
