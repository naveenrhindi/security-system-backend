# Step 1: Build Stage (Using Maven)
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml to the working directory
COPY pom.xml .

# Install the dependencies (this will cache the dependencies layer)
RUN mvn dependency:go-offline

# Copy the source code to the container
COPY src ./src

# Build the application (creates the .jar file)
RUN mvn clean package -DskipTests

# Step 2: Run Stage (Using OpenJDK)
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built .jar file from the build stage
COPY --from=build /app/target/security-system-0.0.1-SNAPSHOT.jar .

# Expose the port your application will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/security-system-0.0.1-SNAPSHOT.jar"]
