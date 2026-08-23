# SISTEMA DE PROCESAMIENTO POR LOTES - BANCO XYZ

Este proyecto implementa una solución de procesamiento por lotes (Batch) utilizando **Spring Batch**, diseñada para modernizar y automatizar los procesos operativos nocturnos de la entidad bancaria, reemplazando sistemas legacy.

---

## CARACTERÍSTICAS PRINCIPALES

El sistema contempla la ejecución independiente de tres jobs principales:

1. **Reporte de Transacciones Diarias:** Lectura de archivos transaccionales en formato plano, validación de reglas de negocio, filtrado de anomalías y generación de un resumen consolidado.
2. **Cálculo de Intereses Mensuales:** Proceso masivo de lectura de cuentas activas, aplicación de tasas de interés según el tipo de producto y actualización persistente en la base de datos.
3. **Generación de Estados de Cuenta Anuales:** Consolidación de información histórica anual por cliente para la auditoría y envío de notificaciones.

---

## TECNOLOGÍAS UTILIZADAS

* **Java** (Versión 17 o superior)
* **Spring Boot**
* **Spring Batch**
* **Maven** o **Gradle** para la gestión de dependencias
* **Base de Datos Relacional** (PostgreSQL / MySQL / Oracle / H2)

---

## ESTRUCTURA DEL PROYECTO

El código está organizado siguiendo una estructura estándar basada en Maven:

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── duoc/
│   │           └── migracion/
│   │               ├── config/       # Configuración de los Jobs (CuentaAnual, Interes, Transaccion)
│   │               ├── controller/   # Controladores REST para disparar los Jobs
│   │               ├── dto/          # Objetos de transferencia de datos desde los CSV
│   │               ├── model/        # Clases de dominio y entidades JPA
│   │               ├── processor/    # Lógica de transformación y reglas de negocio
│   │               ├── reader/       # Componentes de lectura de archivos CSV
│   │               ├── repository/   # Repositorios Spring Data JPA
│   │               ├── writer/       # Componentes de persistencia en base de datos
│   │               └── MigracionBatchApplication.java
│   │
│   └── resources/
│       ├── data/                  # Archivos CSV de entrada (cuentas_anuales, intereses, transacciones)
│       └── application.properties # Propiedades de conexión y configuración batch
│
├── pom.xml                   # Dependencias y configuración de Maven
├── mvnw.cmd                  # Maven Wrapper para Windows
└── README.md                 # Documentación del proyecto

---

## REQUISITOS PREVIOS

1. Tener instalado **JDK 17** o superior.
2. Contar con una base de datos activa (ajustar las credenciales en el archivo `application.properties`).
3. **Maven** o **Gradle** configurado en las variables de entorno del sistema.

---

## INSTRUCCIONES DE EJECUCIÓN

Para compilar el proyecto y ejecutar los procesos por lotes, sigue estos pasos:

1. **Compilar las dependencias** usando Maven Wrapper:
   ```bash
   mvnw clean install