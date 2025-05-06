# Etapa de construcción
FROM maven:3-openjdk-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-slim

WORKDIR /app
COPY --from=build /app/target/FocusPlanner-0.0.1-SNAPSHOT.jar /app/focus-planner.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=online
CMD ["java", "-jar", "/app/focus-planner.jar"]
