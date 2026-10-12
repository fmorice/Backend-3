FROM eclipse-temurin:17-jre
WORKDIR /app
COPY services/pagos/target/pagos-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
