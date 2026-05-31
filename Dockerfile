# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies for caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar file from the build stage
COPY --from=build /app/target/resume-ats-checker-0.0.1-SNAPSHOT.jar app.jar

# Create the uploads directory for handling resume files
RUN mkdir -p uploads

# Expose port and run application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
