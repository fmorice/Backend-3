
# Dockerfile para Exp2_S6_Eureka_Server
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/exp2-s6-eureka-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
