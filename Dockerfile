# Multi-stage Dockerfile for building and running the Spring Boot app

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy only pom first for better layer caching
COPY pom.xml .
# Pre-fetch dependencies (will be cached unless pom changes)
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Build the application (skip tests for faster image builds; run tests in CI)
RUN mvn -B -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
LABEL authors="HP"
WORKDIR /app

# Copy the fat jar from the builder stage
COPY --from=builder /workspace/target/*.jar /app/app.jar

# Render/Heroku-style platforms set $PORT; Spring config already respects ${PORT:8080}
EXPOSE 8080

# JVM performance and container-awareness flags can be added via JAVA_OPTS
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]