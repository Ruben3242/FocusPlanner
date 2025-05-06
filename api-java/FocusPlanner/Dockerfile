# Usar una imagen base con Java
FROM openjdk:17-jdk-slim

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo JAR generado al contenedor
COPY target/FocusPlanner-0.0.1-SNAPSHOT.jar /app/FocusPlanner.jar

# Exponer el puerto 8080
EXPOSE 8080

# Ejecutar el archivo JAR
ENTRYPOINT ["java", "-jar", "FocusPlanner.jar"]
