

## 1. Nombre del proyecto

Banco XYZ – Backend for Frontend (BFF) – Semana 5

## 2. Descripción

Proyecto académico que implementa el patrón Backend For Frontend (BFF). En esta entrega se implementaron tres BFF independientes que adaptan la respuesta del backend interno según las necesidades de cada canal: Web, Móvil y Cajero.

> Nota: el trabajo de Spring Batch de semanas anteriores fue archivado y NO forma parte del ejecutable de esta entrega (ver rama `archive/spring-batch-week4`).

## 3. Objetivo

Cada canal recibe únicamente la información que necesita. Esto evita exponer datos innecesarios y permite aplicar permisos y responsabilidades separadas por canal.

## 4. Tecnologías utilizadas

- Java
- Spring Boot
- Spring Security (HTTP Basic)
- Maven
- HTTPS (TLS) con keystore PKCS12
- JUnit (existen pruebas en el proyecto)

## 5. Arquitectura BFF (resumen)

La aplicación implementa una separación clara entre:

- BFF Web: controlador y DTO específicos en `bff/web`.
- BFF Mobile: controlador y DTO específicos en `bff/mobile`.
- BFF Cajero: controlador y DTO específicos en `bff/cajero`.
- Servicio interno: `AccountService` — carga los datos desde `src/main/resources/data/intereses.csv` y expone la representación interna (`CuentaDto`).
- Seguridad: `SecurityConfig` maneja usuarios en memoria y roles por canal.

Cada BFF consume el servicio interno y transforma la respuesta a su DTO propio (no hay un único endpoint con if/else).

## 6. Endpoints implementados

- GET `/api/bff/web/cuentas/{id}` — BFF Web
- GET `/api/bff/mobile/cuentas/{id}` — BFF Móvil
- GET `/api/bff/cajero/cuentas/{id}` — BFF Cajero
- GET `/api/internal/cuentas/{id}` — devuelve la representación interna (`CuentaDto`).

## 7. Qué devuelve cada BFF (implementación real)

- BFF Web: devuelve los campos `id`, `nombre`, `saldo`, `edad`, `tipo`, `estado`.
- BFF Móvil: devuelve los campos ligeros `id`, `nombre`, `saldo`, `tipo` (NO incluye `edad`).
- BFF Cajero: devuelve los campos mínimos `id`, `saldo`.

## 8. Ejemplos reales (cuenta `101`)

Web (200 OK):

```json
{"id":101,"nombre":"John Doe","saldo":5000.0,"edad":30,"tipo":"ahorro","estado":"ACTIVA"}
```

Móvil (200 OK):

```json
{"id":101,"nombre":"John Doe","saldo":5000.0,"tipo":"ahorro"}
```

Cajero (200 OK):

```json
{"id":101,"saldo":5000.0}
```

Cuenta inexistente (404 Not Found):

```json
{"status":404,"message":"Cuenta no encontrada","path":"/api/bff/web/cuentas/999999"}
```

## 9. Seguridad

- La aplicación expone HTTPS en el puerto `8443` (configurado en `application.properties`).
- Autenticación: HTTP Basic (in-memory users, para pruebas locales).
- Roles y permisos: cada usuario tiene un rol y puede acceder solo a su BFF.

Comportamiento esperado:

- Sin credenciales o credenciales inválidas → `401 Unauthorized`.
- Usuario autenticado pero sin el rol requerido → `403 Forbidden`.
- Cuenta no encontrada → `404 Not Found` (body JSON consistente: `status`, `message`, `path`).

## 10. Usuarios de prueba (solo para pruebas locales)

- `webUser` / `webPass` → `ROLE_WEB`
- `mobileUser` / `mobilePass` → `ROLE_MOBILE`
- `cajeroUser` / `cajeroPass` → `ROLE_CAJERO`

> Estas credenciales están definidas en `src/main/java/com/duoc/migracion/config/SecurityConfig.java` y son únicamente de demostración para pruebas locales.

## 11. HTTPS y keystore (PKCS12)

El proyecto utiliza un keystore PKCS12 para TLS en entorno local. No subir el keystore al repositorio.

Comando para generar un keystore local (ejecutar en tu máquina):

```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore keystore.p12 -validity 3650 -storepass changeit -keypass changeit \
  -dname "CN=localhost, OU=Dev, O=Demo, L=City, ST=State, C=CL"
```

Colocar `keystore.p12` en la raíz del proyecto (ya ignorado por `.gitignore`) y arrancar la app. En `application.properties` están configuradas las propiedades TLS:

```properties
server.port=8443
server.ssl.key-store=keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

Para pruebas con `curl` sobre un certificado autofirmado use `-k` para omitir la verificación del certificado.

## 12. Cómo ejecutar

1. Generar `keystore.p12` (ver sección anterior).
2. Ejecutar tests:

```bash
./mvnw test -q
```

3. Ejecutar la aplicación:

```bash
./mvnw spring-boot:run
```

La aplicación se expondrá en `https://localhost:8443`.

## 13. Pruebas realizadas (resumen)

- Web → 200 OK (ejemplo con cuenta 101, credenciales `webUser:webPass`).
- Mobile → 200 OK (ejemplo con cuenta 101, credenciales `mobileUser:mobilePass`).
- Cajero → 200 OK (ejemplo con cuenta 101, credenciales `cajeroUser:cajeroPass`).
- Cuenta inexistente → 404 Not Found (body JSON consistente).
- Sin autenticación → 401 Unauthorized.
- Usuario autenticado pero rol equivocado → 403 Forbidden.

## 14. Evidencias sugeridas para entrega

Incluir capturas de pantalla o logs que muestren:

1. Inicio de Spring Boot con HTTPS (log mostrando `Tomcat started on port 8443`).
2. Respuesta BFF Web (HTTP 200 + JSON).
3. Respuesta BFF Mobile (HTTP 200 + JSON).
4. Respuesta BFF Cajero (HTTP 200 + JSON).
5. Respuesta 404 para cuenta inexistente (HTTP 404 + JSON).
6. Respuesta 401 (petición sin credenciales).
7. Respuesta 403 (credencial válida con rol equivocado intentando acceder a otro BFF).

## 15. Conclusión

La implementación demuestra el patrón BFF: cada canal recibe una vista adaptada de los datos del backend interno, controlada por controladores y DTOs separados. Además se aplicó autenticación y autorización por rol y se configuró HTTPS para pruebas locales.