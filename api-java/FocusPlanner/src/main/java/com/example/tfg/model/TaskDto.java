package com.example.tfg.model;

import com.example.tfg.enums.Priority;
import com.example.tfg.enums.TaskStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@RequiredArgsConstructor
public class TaskDto {
    private Long id; // Opcional, para importar si se quiere actualizar (puede ser null para crear)
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private TaskStatus status;
    private Priority priority;
}
