package com.example.tfg.repository;

import com.example.tfg.User.TaskStatus;
import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> , JpaSpecificationExecutor<Task> {
    List<Task> findByUser(User user);
    Optional<Task> findByIdAndUser(Long id, User user);
    Page<Task> findByUser(User user, Pageable pageable);
    Page<Task> findByUserAndCompleted(User user, boolean completed, Pageable pageable);

    List<Task> findByUserAndDueDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Task> findByUserAndCompletedTrue(User user);

    List<Task> findByUserAndDueDateBeforeAndCompletedFalse(User user, LocalDate now);

    // Eliminar tareas completadas o expiradas del usuario
    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE (t.completed = true OR t.dueDate < CURRENT_DATE) AND t.user = :user")
    void deleteAllCompletedOrExpiredTasks(@Param("user") User user);

    void deleteByUserAndStatusIn(User user, List<TaskStatus> statuses);

    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}
