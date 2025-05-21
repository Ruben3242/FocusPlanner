package com.example.tfg.controller;

import com.example.tfg.enums.TaskStatus;
import com.example.tfg.enums.Priority;
import com.example.tfg.model.Task;
import com.example.tfg.model.TaskDto;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.example.tfg.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final UserRepository userRepository;
    private final TaskService taskService;
    private final PagedResourcesAssembler<Task> pagedResourcesAssembler;


    // Crear una tarea
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) throws IOException {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Task>> getFilteredTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) TaskStatus status, // Nueva opción para filtrar por estado
            @RequestParam(required = false) Priority priority, // Nueva opción para filtrar por prioridad
            Pageable pageable) {

        List<Task> filteredTasks = taskService.getFilteredTasks(title, completed, dueDate, status, priority, pageable);
        return ResponseEntity.ok(filteredTasks);
    }




    @GetMapping
    public ResponseEntity<PagedModel<Task>> getAllTasks(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

        Page<Task> tasksPage = taskService.getAllTasks(pageable);
        PagedModel<Task> pagedModel = pagedResourcesAssembler.toModel(tasksPage, task -> task);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/tasksbydaterange")
    public ResponseEntity<List<Task>> getTasksByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Task> tasks = taskService.getTasksBetweenDates(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/export")
    public ResponseEntity<List<TaskDto>> exportTasks() {
        Long userId = taskService.getAuthenticatedUser().getId();
        List<TaskDto> exported = taskService.exportTasksForUser(userId);
        return ResponseEntity.ok(exported);
    }

    // Importar tareas (recibe lista de TaskDto)
    @PostMapping("/import")
    public ResponseEntity<String> importTasks(@RequestBody List<TaskDto> tasksDto) {
        Long userId = taskService.getAuthenticatedUser().getId();
        String result = taskService.importTasksForUser(userId, tasksDto);
        return ResponseEntity.ok(result);
    }

    // Obtener una tarea específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar una tarea
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) throws IOException {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    // Eliminar una tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) throws IOException {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/delete-by-status")
    public ResponseEntity<String> deleteTasksByStatuses(@RequestBody List<TaskStatus> statuses) {
        User user = taskService.getAuthenticatedUser();

        taskService.deleteTasksByStatuses(user, statuses);

        // Actualizamos el campo del usuario
        user.setRemoveCompletedExpiredTasks(true);
        userRepository.save(user);

        return ResponseEntity.ok("Tareas eliminadas correctamente.");
    }

}
