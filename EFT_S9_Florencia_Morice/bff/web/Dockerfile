FROM eclipse-temurin:17-jre
WORKDIR /app
COPY bff/web/target/bff-web-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
