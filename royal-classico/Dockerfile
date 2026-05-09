FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Create the directories so the app has a place to save new admin uploads
RUN mkdir -p uploads/players uploads/news uploads/fixtures

# Copy the photos you already have in your project root
COPY uploads/ /app/uploads/

# Copy the built jar from the target folder
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]