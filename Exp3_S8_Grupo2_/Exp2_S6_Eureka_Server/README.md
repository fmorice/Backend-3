# Exp2_S6_Eureka_Server

1. Nombre del proyecto
----------------------

Exp2_S6_Eureka_Server

2. Objetivo
-----------

Proveer un servidor de descubrimiento de servicios (Eureka Server) en modo standalone para la actividad Backend III - Semana 6.

3. Tecnologías y versiones
--------------------------

- Java: 17
- Maven: 3.9.x
- Spring Boot: 3.1.6
- Spring Cloud (release train): 2022.0.5

4. Estructura del proyecto
-------------------------

- pom.xml
- src/main/java/com/bancoxyz/eureka/EurekaServerApplication.java
- src/main/resources/application.properties
- README.md
- .gitignore

5. Requisitos
------------

Instalar Java 17 y Maven 3.9.x en el sistema.

6. Cómo configurar Java 17
-------------------------

En macOS:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

7. Cómo compilar
---------------

Desde la raíz del proyecto:

```bash
mvn -DskipTests=true clean package
# Resultado esperado: BUILD SUCCESS
```

8. Cómo ejecutar
----------------

Ejecutar con Maven:

```bash
mvn spring-boot:run
```

O ejecutar el JAR generado:

```bash
nohup java -jar target/exp2-s6-eureka-server-0.0.1-SNAPSHOT.jar > eureka-server.log 2>&1 &
tail -f eureka-server.log
```

9. Puerto utilizado
-------------------

El servidor Eureka corre en: http://localhost:8761

10. URL del panel de Eureka
--------------------------

http://localhost:8761

11. Cómo comprobar que Eureka está funcionando
---------------------------------------------

- Abrir en el navegador: http://localhost:8761 y verificar que la interfaz se muestra.
- La interfaz puede mostrar inicialmente cero instancias registradas; eso es esperado en esta etapa.

12. Evidencias a capturar
-------------------------

- Salida del comando de compilación: `mvn -DskipTests=true clean package` mostrando `BUILD SUCCESS`.
- Salida del arranque de la app con `mvn spring-boot:run` o `nohup java -jar ...` mostrando `Started EurekaServerApplication` y `Tomcat started on port(s): 8761`.
- Captura de la página `http://localhost:8761` en el navegador mostrando el panel de Eureka.
