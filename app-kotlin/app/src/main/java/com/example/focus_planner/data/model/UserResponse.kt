package com.example.focus_planner.data.model

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isVerified: Boolean
) {
    fun toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            firstname = firstName,
            lastname = lastName,
            isVerified = isVerified,
            verificationToken = null,
            removeCompletedExpiredTasks = false,
            taskList = null,
            role = "ADMIN"
        )
    }
}

