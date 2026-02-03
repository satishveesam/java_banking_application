# Use modern official Java 17 image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy Maven wrapper and config first (better layer caching)
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Give execute permission to mvnw
RUN chmod +x mvnw

# Download dependencies first (faster rebuilds)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the jar
RUN ./mvnw clean package -DskipTests

# Expose port (Render sets PORT env var, but this is fine)
EXPOSE 8081

# Run the app (Render injects PORT automatically)
CMD ["java", "-jar", "target/bankapp-0.0.1-SNAPSHOT.jar"]
