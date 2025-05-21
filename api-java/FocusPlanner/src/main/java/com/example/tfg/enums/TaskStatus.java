package com.example.tfg.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    EXPIRED("EXPIRED"),
    COMPLETED_OR_EXPIRED("COMPLETED_OR_EXPIRED");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

}
