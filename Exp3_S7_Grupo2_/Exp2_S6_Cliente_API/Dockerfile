# Dockerfile para Exp2_S6_Cliente_API
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/Exp2_S6_Cliente_API-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]