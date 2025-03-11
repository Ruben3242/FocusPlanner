package com.example.tfg.service;

import com.example.tfg.User.TaskStatus;
import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.TaskRepository;
import com.example.tfg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.tfg.User.TaskStatus.COMPLETED_OR_EXPIRED;

@Service
@RequiredArgsConstructor
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;
    @Autowired
    private final UserRepository userRepository;

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

    // Crear una nueva tarea
    public Task createTask(Task task) {
        User user = getAuthenticatedUser();
        if (task.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser nula.");
        }

        task.setUser(user);
        return taskRepository.save(task);
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

    public List<Task> getFilteredTasks(TaskStatus status, boolean includeCompleted, boolean includeExpired) {
        User user = getAuthenticatedUser();

        switch (status) {
            case COMPLETED:
                return taskRepository.findByUserAndCompletedTrue(user);
            case EXPIRED:
                return taskRepository.findByUserAndDueDateBeforeAndCompletedFalse(user, LocalDate.now());
            case PENDING:
                return taskRepository.findByUserAndCompletedFalseAndDueDateAfter(user, LocalDate.now());
            case COMPLETED_OR_EXPIRED:
                return taskRepository.findByUserAndCompletedTrueOrDueDateBeforeAndCompletedFalse(user, LocalDate.now());
            default:
                return taskRepository.findByUser(user);
        }
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
