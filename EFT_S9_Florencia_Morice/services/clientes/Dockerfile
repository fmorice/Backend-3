FROM eclipse-temurin:17-jre
WORKDIR /app
COPY services/clientes/target/clientes-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
