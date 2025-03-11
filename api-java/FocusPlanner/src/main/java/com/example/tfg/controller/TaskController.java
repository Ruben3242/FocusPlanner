package com.example.tfg.controller;

import com.example.tfg.User.TaskStatus;
import com.example.tfg.model.Task;
import com.example.tfg.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Crear una tarea
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Task>> getTasksByState(
            @RequestParam boolean completed,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByState(completed, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        // Obtiene todas las tareas paginadas
        Page<Task> tasksPage = taskService.getAllTasks(pageable);

        return ResponseEntity.ok(tasksPage); // Devuelve las tareas paginadas
    }

    @GetMapping("/filtro/{completed}")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(name = "status", defaultValue = "PENDING") TaskStatus status,
            @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted,
            @RequestParam(name = "includeExpired", defaultValue = "false") boolean includeExpired) {
        return ResponseEntity.ok(taskService.   getFilteredTasks(status, includeCompleted, includeExpired));
    }

    // Obtener una tarea específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar una tarea
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    // Eliminar una tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
