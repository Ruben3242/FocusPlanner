package com.example.tfg.service;

import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleCalendarService;
import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleOAuthService;
import com.example.tfg.GogleCalendar.service.GoogleCalendarIntegration;
import com.example.tfg.enums.TaskStatus;
import com.example.tfg.enums.Priority;
import com.example.tfg.model.Task;
import com.example.tfg.model.TaskDto;
import com.example.tfg.model.User;
import com.example.tfg.model.UserTaskStatsDTO;
import com.example.tfg.repository.TaskRepository;
import com.example.tfg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final GoogleCalendarIntegration googleCalendarIntegration;
    @Autowired
    private final GoogleOAuthService googleOAuthService;
    @Autowired
    private final GoogleCalendarService googleCalendarService;


    // Obtener el usuario autenticado
    public User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Obtener todas las tareas del usuario con paginación
    public Page<Task> getAllTasks(Pageable pageable) {
        User user = getAuthenticatedUser();

        // Eliminar tareas completadas y expiradas si el usuario tiene activada esta opción
        if (user.isRemoveCompletedExpiredTasks()) {
            deleteCompletedExpiredTasks(user);
        }

        // Obtener las tareas con paginación
        return taskRepository.findByUser(user, pageable);
    }

    // Eliminar tareas completadas o expiradas
    @Transactional
    public void deleteCompletedExpiredTasks(User user) {
        taskRepository.deleteAllCompletedOrExpiredTasks(user, TaskStatus.COMPLETED_OR_EXPIRED);
    }

    public Page<Task> getTasksByState(boolean completed, Pageable pageable) {
        User user = getAuthenticatedUser();
        return taskRepository.findByUserAndCompleted(user, completed, pageable);
    }

    // Configurable: cantidad de días antes de la fecha de vencimiento para considerar la tarea "próxima a vencer"
    @Value("${task.reminder.daysBeforeDueDate:3}")
    private int reminderDaysBefore;

    public List<Task> getTasksNearDueDate(User user) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(reminderDaysBefore);
        return taskRepository.findByUserAndDueDateBetween(user, today, threshold);
    }

    // Método programado que ejecuta la búsqueda de tareas próximas a vencer
    @Scheduled(cron = "0 0 12 * * ?") // Esto ejecuta el método cada día a las 12:00 PM
    public void checkTasksNearDueDate() {
        User user = getAuthenticatedUser();
        List<Task> tasks = getTasksNearDueDate(user);

        // Aquí podrías añadir la lógica para enviar notificaciones a los usuarios, por ejemplo:
        tasks.forEach(task -> {
            // Lógica de notificación, por ejemplo enviar un email:
            sendReminderNotification(task);
        });
    }

    private void sendReminderNotification(Task task) {
        // Aquí debes agregar el código para enviar el correo o la notificación que necesites
        System.out.println("Recordatorio: La tarea '" + task.getTitle() + "' está próxima a vencer.");
    }

    public List<Task> getFilteredTasks(String title, Boolean completed, LocalDate dueDate, TaskStatus status, Priority priority, Pageable pageable) {
        Specification<Task> spec = Specification.where(null);
        User user = getAuthenticatedUser(); // esto ya lo tienes correctamente

        // 🔐 AÑADIR este filtro para que solo devuelva tareas del usuario autenticado
        spec = spec.and((root, query, cb) -> cb.equal(root.get("user"), user));

        if (title != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (completed != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("completed"), completed));
        }

        if (dueDate != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("dueDate"), dueDate));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        return taskRepository.findAll(spec, pageable).getContent();
    }



    public void deleteCompletedExpiredTasks() {
        User user = getAuthenticatedUser();
        boolean removeCompletedExpiredTasks = user.isRemoveCompletedExpiredTasks();

        if (removeCompletedExpiredTasks) {
            // Eliminar tareas completadas
            List<Task> completedTasks = taskRepository.findByUserAndCompletedTrue(user);
            taskRepository.deleteAll(completedTasks);

            // Eliminar tareas expiradas
            List<Task> expiredTasks = taskRepository.findByUserAndDueDateBeforeAndCompletedFalse(user, LocalDate.now());
            taskRepository.deleteAll(expiredTasks);
        }
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

    public Task createTask(Task task) throws IOException {
        User user = getAuthenticatedUser();
        task.setUser(user);
        task.setStatus(calculateStatus(task));
        Task savedTask = taskRepository.save(task);

        try {
            String eventId = googleCalendarService.createCalendarEvent(savedTask, user);
            savedTask.setGoogleCalendarEventId(eventId);
            taskRepository.save(savedTask); // actualizar con ID del evento
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedTask;
    }

    public Task updateTask(Long id, Task updatedTask) throws IOException {
        User user = getAuthenticatedUser();
        Task existingTask = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        boolean wasCompleted = existingTask.isCompleted(); // estado ANTES del cambio
        boolean nowCompleted = updatedTask.isCompleted();  // nuevo valor

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(calculateStatus(existingTask));
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setGoogleCalendarEventId(existingTask.getGoogleCalendarEventId()); // no tocar

        existingTask.setCompleted(nowCompleted); // ahora sí se actualiza

        if (!wasCompleted && nowCompleted) {
            existingTask.setCompletedAt(LocalDateTime.now());
        } else if (wasCompleted && !nowCompleted) {
            existingTask.setCompletedAt(null);
        }


        Task savedTask = taskRepository.save(existingTask);

        try {
            if (user.getGoogleAccessToken() != null && savedTask.getGoogleCalendarEventId() != null) {
                googleCalendarService.updateCalendarEvent(savedTask, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedTask;
    }


    public void deleteTask(Long id) throws IOException {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        try {
            if (user.getGoogleAccessToken() != null && task.getGoogleCalendarEventId() != null) {
                googleCalendarService.deleteCalendarEvent(task.getGoogleCalendarEventId(), user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        taskRepository.delete(task);
    }

    public List<Task> getTasksBetweenDates(LocalDate start, LocalDate end) {
        User user = getAuthenticatedUser();
        return taskRepository.findByUserAndDueDateBetween(user, start, end);
    }

    private TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setCompleted(task.isCompleted());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        return dto;
    }

    // Convierte TaskDto a Task
    private Task fromDto(TaskDto dto, Long userId) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setCompleted(dto.isCompleted());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id " + userId));

        task.setUser(user);
        return task;
    }


    // Exportar tareas de un usuario
    public List<TaskDto> exportTasksForUser(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Importar tareas para un usuario
    public String importTasksForUser(Long userId, List<TaskDto> taskDtos) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id " + userId));

        int addedCount = 0;
        int skippedCount = 0;

        for (TaskDto dto : taskDtos) {
            // Comprobar si ya existe una tarea con el mismo título para este usuario
            boolean exists = taskRepository.existsByUserIdAndTitle(userId, dto.getTitle());

            if (exists) {
                skippedCount++;
            } else {
                Task task = new Task();
                // No seteamos ID para que se genere nuevo
                task.setTitle(dto.getTitle());
                task.setDescription(dto.getDescription());
                task.setDueDate(dto.getDueDate());
                task.setCompleted(dto.isCompleted());
                task.setStatus(dto.getStatus());
                task.setPriority(dto.getPriority());
                task.setUser(user);

                Task savedTask = taskRepository.save(task);
                try {
                    String eventId = googleCalendarService.createCalendarEvent(savedTask, user);
                    savedTask.setGoogleCalendarEventId(eventId);
                    taskRepository.save(savedTask);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addedCount++;
            }
        }

        return addedCount + " tareas importadas, " + skippedCount + " tareas duplicadas e ignoradas.";
    }
    @Transactional
    public void deleteTasksByStatuses(User user, List<TaskStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return;
        }

        if (statuses.contains(TaskStatus.COMPLETED_OR_EXPIRED)) {
            taskRepository.deleteAllCompletedOrExpiredTasks(user, TaskStatus.COMPLETED_OR_EXPIRED);
            return;
        }

        taskRepository.deleteByUserAndStatusIn(user, statuses);
    }

    private TaskStatus calculateStatus(Task task) {
        if (task.isCompleted()) {
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
                return TaskStatus.COMPLETED_OR_EXPIRED;
            }
            return TaskStatus.COMPLETED;
        } else if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            return TaskStatus.EXPIRED;
        } else {
            return TaskStatus.PENDING;
        }
    }

    public UserTaskStatsDTO getUserTaskStats(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);

        int total = tasks.size();
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();

        // Obtener la hora más productiva
        Map<Integer, Long> hourCount = tasks.stream()
                .filter(Task::isCompleted)
                .map(task -> {
                    if (task.getCompletedAt() != null) {
                        return task.getCompletedAt().getHour();
                    } else if (task.getCreatedAt() != null) {
                        return task.getCreatedAt().getHour();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));

        Integer mostProductiveHour = hourCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new UserTaskStatsDTO(total, completed, mostProductiveHour);
    }

}
