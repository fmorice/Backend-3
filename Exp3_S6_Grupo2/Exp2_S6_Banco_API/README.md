# Exp2_S6_Banco_API

## 1. Nombre del proyecto

Exp2_S6_Banco_API

## 2. Objetivo

Microservicio REST para Banco XYZ que:

- consume configuración centralizada desde Config Server;
- se registra en Eureka Service Discovery;
- utiliza autenticación y autorización con Spring Security (HTTP Basic);
- implementa tolerancia a fallos mediante Resilience4j / Circuit Breaker.

## 3. Tecnologías y versiones

- Java: 17
- Maven: 3.9.x
- Spring Boot: 3.1.6
- Spring Cloud: 2022.0.5

## 4. Servicios y puertos

- Config Server: http://localhost:8888
- Eureka Server: http://localhost:8761
- Banco API: http://localhost:8080

> Nota: No se modificaron versiones ni puertos del proyecto.

## 5. Configuración centralizada (Config Server)

Banco API importa configuración de Config Server mediante la propiedad:

```
spring.config.import=configserver:http://localhost:8888
```

La propiedad de ejemplo que se utiliza en el proyecto es `example.message`. El endpoint `GET /api/banco/config` devuelve el valor obtenido desde Config Server, y sirve para verificar que la aplicación está consumiendo la configuración centralizada correctamente.

## 6. Service Discovery (Eureka)

La aplicación se registra en Eureka con el nombre de aplicación `BANCO-XYZ`. Puedes verificar el registro en `http://localhost:8761`.

## 7. Seguridad (Spring Security)

Se implementó autenticación HTTP Basic con usuarios en memoria:

- Usuario: `usuario` / Contraseña: `usuario123` — rol `USER`
- Usuario: `admin` / Contraseña: `admin123` — rol `ADMIN`

CSRF está deshabilitado para facilitar pruebas de la API.

Rutas protegidas y públicas:

- `/api/banco/health` — público
- `/api/banco/config` — requiere autenticación
- `/api/banco/seguro` — requiere autenticación (devuelve información del usuario autenticado)

## 8. Resiliencia (Resilience4j)

Se añadió un ejemplo de Circuit Breaker con Resilience4j. El endpoint de prueba es `GET /api/banco/resiliencia`.

- Respuesta normal:

```json
{
	"estado": "OK",
	"mensaje": "Servicio respondió correctamente"
}
```

- Para provocar una falla controlada y activar el fallback:

`GET /api/banco/resiliencia?fail=true`

- Respuesta de fallback:

```json
{
	"estado": "FALLBACK",
	"mensaje": "Servicio temporalmente no disponible. Se ejecutó el mecanismo de respaldo."
}
```

## 9. Estructura del proyecto

```
Exp2_S6_Banco_API/
├── pom.xml
├── README.md
├── .gitignore
└── src/
		└── main/
				├── java/
				│   └── com/
				│       └── bancoxyz/
				│           └── banco/
				│               ├── BancoApiApplication.java
				│               ├── BancoController.java
				│               ├── SecurityConfig.java
				│               └── BancoResilienciaService.java
				│
				└── resources/
						└── application.properties
```

## 10. Cómo preparar el entorno (Java 17)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 11. Cómo compilar y ejecutar

Compilar:

```bash
mvn -DskipTests=true clean package
```

Ejecutar:

```bash
mvn spring-boot:run
```

> Orden recomendado de arranque en entorno local:
1. Levantar Config Server en `8888`
2. Levantar Eureka Server en `8761`
3. Levantar Banco API en `8080`

## 12. Endpoints (resumen)

| Método | Endpoint | Autenticación | Descripción |
|---|---:|---|---|
| GET | `/api/banco/health` | No | Comprueba que Banco API está levantado. Devuelve un JSON con estado. |
| GET | `/api/banco/config` | Sí (HTTP Basic) | Retorna la propiedad `example.message` obtenida desde Config Server. Sin credenciales responde `401 Unauthorized`. |
| GET | `/api/banco/seguro` | Sí (HTTP Basic) | Endpoint protegido que devuelve un JSON con `mensaje: "Acceso autorizado"` y el usuario autenticado. |
| GET | `/api/banco/resiliencia` | No (ejemplo) | Endpoint que invoca un servicio interno protegido por Circuit Breaker; respuesta normal `OK`. |
| GET | `/api/banco/resiliencia?fail=true` | No (ejemplo) | Forzar falla controlada para activar el fallback; devuelve JSON con `estado: FALLBACK`. |

## 13. Evidencias de ejecución

Coloca capturas donde se indica si necesitas incluir imágenes. No se agregan imágenes ni rutas en este README.

### Evidencia 1 — Config Server

La siguiente evidencia muestra que Banco API, ejecutándose en `localhost:8080`, obtiene correctamente una propiedad desde el Config Server, ejecutándose en `localhost:8888`. La respuesta se obtiene mediante el endpoint `/api/banco/config`.

[Insertar captura: Evidencia 1 - Config Server]

Esta respuesta demuestra el consumo de configuración centralizada (`example.message`).

### Evidencia 2 — Service Discovery

La siguiente evidencia muestra el registro exitoso del microservicio `BANCO-XYZ` en Eureka Server. El estado `UP` indica que el microservicio se encuentra registrado y disponible.

[Insertar captura: Evidencia 2 - Eureka Service Discovery]

### Evidencia 3 — Autenticación rechazada

La siguiente evidencia muestra el acceso al endpoint protegido sin proporcionar credenciales. El servidor responde con `401 Unauthorized`, demostrando que Spring Security bloquea el acceso no autenticado.

[Insertar captura: Evidencia 3 - 401 Unauthorized]

### Evidencia 4 — Autenticación correcta

La siguiente evidencia muestra el acceso utilizando credenciales válidas. El servidor responde con `200` y permite el acceso autenticado.

[Insertar captura: Evidencia 4 - Autenticación correcta 200]

### Evidencia 5 — Endpoint protegido

La siguiente evidencia corresponde al endpoint `/api/banco/seguro`, accedido mediante autenticación. El sistema identifica al usuario autenticado y devuelve el mensaje `Acceso autorizado`.

[Insertar captura: Evidencia 5 - /api/banco/seguro]

### Evidencia 6 — Tolerancia a fallos

La siguiente evidencia muestra la ejecución del endpoint `/api/banco/resiliencia?fail=true`, provocando una falla controlada. El sistema activa el mecanismo de respaldo (fallback) y devuelve el estado `FALLBACK`, demostrando el funcionamiento de la tolerancia a fallos mediante Resilience4j.

[Insertar captura: Evidencia 6 - Resiliencia FALLBACK]

## 14. Resultado de la implementación

El microservicio ahora:

- consume configuración centralizada desde Config Server (`example.message`);
- se registra en Eureka como `BANCO-XYZ`;
- protege endpoints mediante Spring Security (HTTP Basic) y usuarios en memoria;
- permite acceso autenticado a endpoints protegidos (`/api/banco/config`, `/api/banco/seguro`);
- implementa tolerancia a fallos con Resilience4j y un mecanismo de fallback;
- fue compilado correctamente con `mvn -DskipTests=true clean package` mostrando `BUILD SUCCESS`.

---

Si necesitas que añada ejemplos de `curl` concretos para cada evidencia (salvo las capturas), puedo pegarlos a continuación.

