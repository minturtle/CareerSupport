# 1. Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the current directory contents into the container at /app
COPY app/app.jar app.jar

# 4. Make port 8080 available to the world outside this container
EXPOSE 8080

# 5. Run the Spring Boot application
ENTRYPOINT ["java","-jar","/app/app.jar"]
