# Multi-stage build
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /build

# Copy parent pom and dependencies
COPY ../pom.xml .
COPY ../common-dto ./common-dto
COPY ../keycloak-provider ./keycloak-provider

# Copy task service
COPY . .

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
