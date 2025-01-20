package com.example.tfg;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {

    public static void main(String[] args) {
        Properties properties = new Properties();

        try {
            // Cargar las propiedades desde el archivo
            properties.load(new FileInputStream("src/main/resources/application.properties"));

            // Obtener la URL de la base de datos desde las propiedades
            String databaseUrl = properties.getProperty("database.url");

            // Si no se carga desde el archivo, usar la variable de entorno directamente
            if (databaseUrl == null) {
                databaseUrl = System.getenv("DATABASE_URL");
            }

            // Establecer la conexión con la base de datos
            Connection connection = DriverManager.getConnection(databaseUrl);

            // Verificar la conexión ejecutando una consulta simple
            if (connection != null) {
                System.out.println("Conexión exitosa a la base de datos.");

                // Realizar una consulta simple (verificar si la base de datos está respondiendo)
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    if (rs.next()) {
                        System.out.println("La base de datos ha respondido correctamente.");
                    }
                } catch (SQLException e) {
                    System.out.println("Error al ejecutar la consulta en la base de datos.");
                    e.printStackTrace();
                }
            } else {
                System.out.println("No se pudo establecer la conexión a la base de datos.");
            }

            // Cerrar la conexión
            connection.close();

        } catch (IOException e) {
            System.out.println("Error al cargar las propiedades del archivo.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }
}
