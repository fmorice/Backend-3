
# Dockerfile para Exp2_S6_Banco_API
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/exp2-s6-banco-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
