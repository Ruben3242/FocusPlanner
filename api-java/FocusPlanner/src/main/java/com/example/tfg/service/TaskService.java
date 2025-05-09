package com.example.tfg.service;

import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleCalendarService;
import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleOAuthService;
import com.example.tfg.GogleCalendar.service.GoogleCalendarIntegration;
import com.example.tfg.enums.TaskStatus;
import com.example.tfg.enums.Priority;
import com.example.tfg.model.Task;
import com.example.tfg.model.User;
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
import java.util.List;
import java.util.Optional;

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
    private User getAuthenticatedUser() {
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
        taskRepository.deleteAllCompletedOrExpiredTasks(user);
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

//    public Task updateTask(Long id, Task taskDetails) {
//        User user = getAuthenticatedUser(); // Obtener el usuario autenticado
//        Task task = taskRepository.findByIdAndUser(id, user)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
//
//        if (taskDetails.getDueDate() == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser nula.");
//        }
//
//        // Actualizar los campos de la tarea
//        task.setTitle(taskDetails.getTitle());
//        task.setDescription(taskDetails.getDescription());
//        task.setCompleted(taskDetails.isCompleted());
//        task.setDueDate(taskDetails.getDueDate());
//
//        // Si la prioridad es proporcionada, actualizarla
//        if (taskDetails.getPriority() != null) {
//            task.setPriority(taskDetails.getPriority());
//        }
//
//        return taskRepository.save(task);
//    }


    // Eliminar una tarea solo si pertenece al usuario autenticado
//    public void deleteTask(Long id) {
//        User user = getAuthenticatedUser();
//        Task task = taskRepository.findByIdAndUser(id, user)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
//        taskRepository.delete(task);
//    }

    public Task createTask(Task task) throws IOException {
        User user = getAuthenticatedUser();
        task.setUser(user);
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

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setCompleted(updatedTask.isCompleted());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setGoogleCalendarEventId(updatedTask.getGoogleCalendarEventId());

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
}
