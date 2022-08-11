FROM openjdk:11

EXPOSE 80

ADD target/food-tracker-api-0.0.1-SNAPSHOT.jar food-tracker-api.jar

ENTRYPOINT ["java", "-jar","food-tracker-api.jar"]