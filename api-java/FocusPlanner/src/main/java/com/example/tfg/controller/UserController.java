package com.example.tfg.controller;


import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.service.TaskService;
import com.example.tfg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TaskService taskService;

//    @PostMapping("/register")
//    public ResponseEntity<String> registerUser(@RequestBody User user) {
//        userDetailsService.save(user); // Guarda el usuario
//        return ResponseEntity.ok("User registered successfully!");
//    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);  // Llama al servicio que obtiene el usuario por ID
        if (user != null) {
            return ResponseEntity.ok(user);  // Esto devuelve un objeto User, que Spring convierte a JSON automáticamente
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


   /* @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setPassword(user.getPassword());
            existingUser.setTasks(user.getTasks());
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
        }
        for (Task task : user.getTasks()) {
            task.setUser(user);
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    */

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Actualizar los campos básicos del usuario
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());

                    // Si la contraseña ha sido proporcionada, se debe codificar
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword())); // Codificar la contraseña
                    }

                    // Actualizar el campo removeCompletedExpiredTasks
                    existingUser.setRemoveCompletedExpiredTasks(userDetails.isRemoveCompletedExpiredTasks());

                    // Si removeCompletedExpiredTasks es true, eliminar las tareas completadas o expiradas
                    if (userDetails.isRemoveCompletedExpiredTasks()) {
                        taskService.deleteCompletedExpiredTasks(existingUser); // Llamar al servicio para eliminar tareas
                    }

                    // Si las tareas han sido proporcionadas, actualizarlas
                    if (userDetails.getTasks() != null) {
                        // Limpiar las tareas actuales
                        existingUser.getTasks().clear();

                        // Asignar las nuevas tareas y validar que no sean null los campos necesarios
                        for (Task task : userDetails.getTasks()) {
                            if (task.getDueDate() == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Due date cannot be null for tasks");
                            }

                            task.setUser(existingUser);  // Asociar el usuario con la tarea
                            existingUser.getTasks().add(task);  // Agregar la tarea a la lista
                        }
                    }

                    // Guardar el usuario actualizado
                    User updatedUser = userRepository.save(existingUser);

                    // Retornar la respuesta con el usuario actualizado
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")); // Si no se encuentra el usuario
    }




//    @PutMapping("/{id}")
//    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
//        return userRepository.findById(id)
//                .map(user -> {
//                    user.setUsername(userDetails.getUsername());
//                    user.setEmail(userDetails.getEmail());
//                    user.setPassword(userDetails.getPassword());
//                    return ResponseEntity.ok(userRepository.save(user));
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElse(ResponseEntity.status(404).body("User not found"));
    }

//    @GetMapping("/test-email")
//    public String sendTestEmail() {
//        testEmailService.sendTestEmail();
//        return "Correo de prueba enviado. Revisa la consola para errores.";
//    }

}
