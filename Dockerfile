# Step 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

# Copy source code and build the JAR
COPY src src
RUN ./mvnw clean package -DskipTests

# Step 2: Create runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (for local runs; Cloud Run injects $PORT)
EXPOSE 8080

# Cloud Run requires listening on $PORT
ENV PORT=8080

# Run the app
CMD ["java", "-jar", "app.jar"]
