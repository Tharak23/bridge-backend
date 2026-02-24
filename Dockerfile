# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached unless pom changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN adduser -D appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Render sets PORT; default 8080 for local
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]
