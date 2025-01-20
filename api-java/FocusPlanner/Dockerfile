FROM openjdk:17-slim

WORKDIR /app

# Copiar el JAR al contenedor
COPY target/FocusPlanner-0.0.1-SNAPSHOT.jar /app/focus-planner.jar

# Exponer el puerto 8080
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=online

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "/app/focus-planner.jar"]
