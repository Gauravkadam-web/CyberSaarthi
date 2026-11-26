# Multi-stage Dockerfile for CyberSaarthi Spring Boot Backend
# Stage 1: Build JAR using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java 21 Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Non-root user for security
RUN addgroup --system cybersaarthi && adduser --system --ingroup cybersaarthi cybersaarthi
USER cybersaarthi:cybersaarthi

COPY --from=builder /workspace/target/*.jar app.jar

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
