FROM eclipse-temurin:17-jre
WORKDIR /app
COPY bff/atm/target/bff-atm-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
