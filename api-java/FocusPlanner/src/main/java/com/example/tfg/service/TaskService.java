package com.example.tfg.service;

import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.TaskRepository;
import com.example.tfg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Task createTask(Task task) {
        User user = getAuthenticatedUser();
        task.setUser(user);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        User user = getAuthenticatedUser();
        return taskRepository.findByUser(user);
    }

    public Optional<Task> getTaskById(Long id) {
        User user = getAuthenticatedUser();
        return taskRepository.findByIdAndUser(id, user);
    }

    public Task updateTask(Long id, Task taskDetails) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setDueDate(taskDetails.getDueDate());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }
}
