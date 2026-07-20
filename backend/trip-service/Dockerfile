# Build context = backend/ (see infra/docker-compose.yml)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY trip-service/pom.xml /workspace/trip-service/pom.xml
RUN mvn -q -f /workspace/pom.xml -pl trip-service -am dependency:go-offline || true
COPY . /workspace
RUN mvn -q -f /workspace/pom.xml -pl trip-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/trip-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
