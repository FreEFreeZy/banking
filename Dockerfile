FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:23-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=builder --chown=spring:spring /app/target/banksystem-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]