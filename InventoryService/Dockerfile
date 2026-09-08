# =========================
# Build stage
# =========================
FROM eclipse-temurin:23-jdk AS build

WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -DskipTests

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Inventory Service port
EXPOSE 8081

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]