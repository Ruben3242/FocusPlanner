package com.example.tfg.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserTaskStatsDTO {
    private int total;
    private int completed;
    private Integer mostProductiveHour;

    public UserTaskStatsDTO(int total, int completed, Integer mostProductiveHour) {
        this.total = total;
        this.completed = completed;
        this.mostProductiveHour = mostProductiveHour;
    }


}
