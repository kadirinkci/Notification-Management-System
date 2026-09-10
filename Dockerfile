FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw -B -DskipTests package


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S application \
    && adduser -S application -G application

COPY --from=build /workspace/target/*.jar app.jar

USER application

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
