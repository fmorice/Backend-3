FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl batch/batch-jobs -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/batch/batch-jobs/target/batch-jobs-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
