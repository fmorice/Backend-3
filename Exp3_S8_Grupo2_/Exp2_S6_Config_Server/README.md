# Exp2_S6_Config_Server

## 1. Nombre del proyecto

**Exp2_S6_Config_Server**

## 2. Objetivo

Proveer un **Spring Cloud Config Server** en modo `native`, que permita centralizar y servir configuraciones externas desde la carpeta local `config-repo`.

Este proyecto corresponde a la actividad de **Backend III - Semana 6** y utiliza una configuración local para almacenar y entregar propiedades de la aplicación `banco-xyz`.

## 3. Tecnologías y versiones

* **Java:** 17.0.19
* **Maven:** 3.9.14
* **Spring Boot:** 3.1.6
* **Spring Cloud:** 2022.0.5
* **Spring Cloud Config Server**
* **Spring Boot Actuator**

## 4. Estructura del proyecto

La estructura principal del proyecto es:

```text
Exp2_S6_Config_Server/
│
├── pom.xml
├── README.md
│
├── config-repo/
│   └── banco-xyz.yml
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── bancoxyz/
        │           └── configserver/
        │               └── ConfigServerApplication.java
        │
        └── resources/
            └── application.properties
```

### Archivos principales

* `pom.xml`: contiene las dependencias y versiones del proyecto.
* `ConfigServerApplication.java`: clase principal de Spring Boot con `@EnableConfigServer`.
* `application.properties`: configura el puerto, el perfil `native` y la ubicación del repositorio local.
* `config-repo/banco-xyz.yml`: contiene las propiedades externas de ejemplo para la aplicación `banco-xyz`.
* `README.md`: documentación del proyecto y pasos para ejecutarlo.

## 5. Configuración de Java 17

En macOS se puede seleccionar Java 17 con:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Para comprobar la versión:

```bash
java -version
```

Debe mostrar una versión 17.x.

También se puede verificar la versión utilizada por Maven:

```bash
mvn -version
```

Debe indicar:

```text
Java version: 17.0.19
```

## 6. Compilación del proyecto

Desde la carpeta raíz del proyecto ejecutar:

```bash
mvn -DskipTests=true clean package
```

El resultado esperado es:

```text
BUILD SUCCESS
```

El proceso genera el archivo JAR dentro de la carpeta `target`:

```text
target/exp2-s6-config-server-0.0.1-SNAPSHOT.jar
```

## 7. Ejecución del Config Server

### Opción 1: Ejecutar con Maven

Con Java 17 configurado previamente:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Luego ejecutar:

```bash
mvn spring-boot:run
```

El servidor se inicia utilizando el perfil `native` configurado en `application.properties`.

El servidor queda disponible en:

```text
http://localhost:8888
```

### Opción 2: Ejecutar el archivo JAR

Después de realizar la compilación:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Ejecutar:

```bash
java -jar target/exp2-s6-config-server-0.0.1-SNAPSHOT.jar
```

También se puede ejecutar en segundo plano:

```bash
nohup java -jar target/exp2-s6-config-server-0.0.1-SNAPSHOT.jar > config-server.log 2>&1 &
```

Para revisar los mensajes de inicio:

```bash
tail -f config-server.log
```

## 8. Configuración local

El proyecto utiliza Spring Cloud Config Server en modo `native`.

Las configuraciones se almacenan en:

```text
config-repo/
```

El archivo de ejemplo es:

```text
config-repo/banco-xyz.yml
```

Su contenido es:

```yaml
spring:
  application:
    name: banco-xyz

example:
  message: "Propiedad de ejemplo desde el repo de configuración"
```

La configuración permite que el Config Server encuentre este archivo mediante:

```properties
spring.cloud.config.server.native.searchLocations=classpath:/config-repo,./config-repo
```

## 9. Endpoint de prueba

Para consultar la configuración de `banco-xyz` utilizando el perfil `default`, se puede acceder a:

```text
http://localhost:8888/banco-xyz/default
```

También se puede probar desde la terminal con:

```bash
curl http://localhost:8888/banco-xyz/default
```

## 10. Ejemplo de respuesta

Si el servidor está funcionando correctamente, el endpoint devuelve una respuesta similar a:

```json
{
  "name": "banco-xyz",
  "profiles": ["default"],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:config-repo/banco-xyz.yml",
      "source": {
        "spring.application.name": "banco-xyz",
        "example.message": "Propiedad de ejemplo desde el repo de configuración"
      }
    }
  ]
}
```

Esta respuesta demuestra que el Config Server pudo encontrar y entregar la configuración almacenada en `config-repo/banco-xyz.yml`.

## 11. Evidencias para la entrega

Se recomienda presentar las siguientes evidencias:

### Evidencia 1: Compilación exitosa

Captura de la terminal ejecutando:

```bash
mvn -DskipTests=true clean package
```

Debe visualizarse:

```text
BUILD SUCCESS
```

### Evidencia 2: Servidor iniciado

Captura de la terminal mostrando mensajes similares a:

```text
Started ConfigServerApplication
Tomcat started on port(s): 8888
```

También debería visualizarse que el perfil `native` está activo.

### Evidencia 3: Endpoint funcionando

Ejecutar:

```bash
curl http://localhost:8888/banco-xyz/default
```

La captura debe mostrar el JSON con la configuración de `banco-xyz`.

### Evidencia 4: Repositorio de configuración

Mostrar la carpeta:

```bash
ls -la config-repo
```

Y el contenido del archivo:

```bash
cat config-repo/banco-xyz.yml
```

Esto permite demostrar que la configuración entregada por el Config Server proviene del repositorio local.

## 12. Configuración principal

El archivo `application.properties` contiene la configuración principal del servidor:

```properties
server.port=8888
spring.application.name=config-server
spring.cloud.config.server.native.searchLocations=classpath:/config-repo,./config-repo
spring.profiles.active=native
management.endpoints.web.exposure.include=*
```

## 13. Resultado final

El proyecto queda configurado para:

* Ejecutarse con **Java 17**.
* Utilizar **Maven 3.9.14**.
* Ejecutar **Spring Boot 3.1.6**.
* Utilizar **Spring Cloud 2022.0.5**.
* Funcionar como **Spring Cloud Config Server**.
* Utilizar el perfil `native`.
* Leer configuraciones desde la carpeta local `config-repo`.
* Ejecutarse en el puerto `8888`.
* Entregar la configuración de `banco-xyz` mediante el endpoint:

```text
http://localhost:8888/banco-xyz/default
```

La compilación fue validada correctamente con `BUILD SUCCESS` y el endpoint fue probado obteniendo la configuración almacenada en `config-repo/banco-xyz.yml`.
