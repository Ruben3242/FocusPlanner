package com.example.tfg.service;

import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.TaskRepository;
import com.example.tfg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Obtener el usuario autenticado
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // Crear una nueva tarea
    public Task createTask(Task task) {
        User user = getAuthenticatedUser();
        if (task.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser nula.");
        }

        task.setUser(user);
        return taskRepository.save(task);
    }

    // Obtener todas las tareas del usuario autenticado
    public List<Task> getAllTasks() {
        User user = getAuthenticatedUser();
        return taskRepository.findByUser(user);
    }

    // Obtener una tarea por ID, solo si pertenece al usuario autenticado
    public Optional<Task> getTaskById(Long id) {
        User user = getAuthenticatedUser();
        return taskRepository.findByIdAndUser(id, user);
    }

    // Actualizar una tarea si pertenece al usuario autenticado
    public Task updateTask(Long id, Task taskDetails) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        if (taskDetails.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser nula.");
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setDueDate(taskDetails.getDueDate());

        return taskRepository.save(task);
    }

    // Eliminar una tarea solo si pertenece al usuario autenticado
    public void deleteTask(Long id) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        taskRepository.delete(task);
    }
}
