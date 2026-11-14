# Image of the REST API.
#
# The build context is the module food-tracker-api, so the image reads the JAR
# file out of the real output folder of Maven. The first version read the JAR
# out of a copy under src/main/docker/target, and that copy went into the
# repository.
#
# Build the image:
#   cd backend/food-tracker-api
#   sh ./mvnw clean package
#   docker build -f src/main/docker/Dockerfile -t food-tracker-api .
FROM eclipse-temurin:17-jre

# The Android client and docker-compose.yml expect this port.
EXPOSE 80

ARG JAR_FILE=target/food-tracker-api-*.jar
COPY ${JAR_FILE} food-tracker-api.jar

ENTRYPOINT ["java", "-jar", "food-tracker-api.jar"]
