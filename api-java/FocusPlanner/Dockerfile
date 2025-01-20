# Usar una imagen base de Maven para construir el proyecto
FROM maven:3-openjdk-17 AS build

WORKDIR /app

# Copiar el código fuente al contenedor
COPY . .

# Construir el archivo JAR usando Maven
RUN mvn clean package -DskipTests

# Usar una imagen base de OpenJDK para ejecutar la aplicaci
FROM openjdk:17-slim

WORKDIR /app

# Copiar el archivo JAR del contenedor de construcción al contenedor de ejecución
COPY --from=build /app/target/FocusPlanner-0.0.1-SNAPSHOT.jar /app/focus-planner.jar

# Exponer el puerto 8080
EXPOSE 8080

# Configuración del perfil activo (Online)
ENV SPRING_PROFILES_ACTIVE=online

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "/app/focus-planner.jar"]
