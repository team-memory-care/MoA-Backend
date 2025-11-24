FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/MoA-Backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

