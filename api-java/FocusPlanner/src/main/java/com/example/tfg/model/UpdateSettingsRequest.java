package com.example.tfg.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class UpdateSettingsRequest {

    private boolean removeCompletedExpiredTasks;

    public boolean getRemoveCompletedExpiredTasks() {
        return removeCompletedExpiredTasks;
    }
}
