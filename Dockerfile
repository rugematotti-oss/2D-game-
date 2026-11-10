# Builds the project and runs the tests with no screen needed.
# The game window itself runs on the host unless staff says otherwise.
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
CMD ["mvn", "-q", "test"]
