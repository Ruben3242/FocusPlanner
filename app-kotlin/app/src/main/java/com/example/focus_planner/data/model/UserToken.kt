package com.example.focus_planner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tokens")
data class UserToken(
    @PrimaryKey val email: String,
    val token: String,
    val expiryTime: Long,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val isVerified: Boolean
)
