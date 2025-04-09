package com.example.focus_planner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tokens")
data class UserToken(
    @PrimaryKey val email: String,
    val token: String,
    val expiryTime: Long
)
