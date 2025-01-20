package com.example.tfg.controller;

import com.example.tfg.repository.UserRepository;  // Asegúrate de tener un repositorio adecuado.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;  // Reemplaza esto con el repositorio que estás utilizando

    @GetMapping("/test-db")
    public String testDatabaseConnection() {
        try {
            // Realiza una simple consulta para probar la conexión
            long count = userRepository.count();  // Método simple de ejemplo, ajusta según tu entidad.
            return "Conexión a la base de datos exitosa. Registros encontrados: " + count;
        } catch (Exception e) {
            return "Error al conectar con la base de datos: " + e.getMessage();
        }
    }
}
