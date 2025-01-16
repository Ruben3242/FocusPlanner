package com.example.tfg.controller;

import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
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
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updaterUser(@PathVariable Long id, @RequestBody User updatedUser) {
        // Buscar el usuario por ID
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = optionalUser.get();

        // Actualizar los datos básicos del usuario
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());

        // Asociar las tareas al usuario
        for (Task task : updatedUser.getTasks()) {
            task.setUser(existingUser); // Asignar el usuario a cada tarea
        }
        existingUser.getTasks().clear(); // Limpiar las tareas actuales
        existingUser.getTasks().addAll(updatedUser.getTasks()); // Agregar las nuevas tareas

        // Guardar el usuario actualizado
        User savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }





    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setPassword(userDetails.getPassword());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElse(ResponseEntity.status(404).body("User not found"));
    }

}
