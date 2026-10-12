FROM eclipse-temurin:17-jre
WORKDIR /app
COPY services/cuentas/target/cuentas-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
