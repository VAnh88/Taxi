# Build context = backend/ (see infra/docker-compose.yml)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY auth-service/pom.xml /workspace/auth-service/pom.xml
RUN mvn -q -f /workspace/pom.xml -pl auth-service -am dependency:go-offline || true
COPY . /workspace
RUN mvn -q -f /workspace/pom.xml -pl auth-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
