
# Dockerfile para Exp2_S6_Config_Server
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/exp2-s6-config-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
