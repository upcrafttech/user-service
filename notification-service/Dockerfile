# Multi-stage build
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /build

COPY ../pom.xml .
COPY ../common-dto ./common-dto

COPY . .

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
