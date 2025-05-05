package com.example.focus_planner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focus_planner.data.model.UserToken

@Dao
interface UserTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userToken: UserToken)

    @Query("SELECT * FROM user_tokens WHERE email = :email LIMIT 1")
    suspend fun getTokenByEmail(email: String): UserToken?

    @Query("DELETE FROM user_tokens WHERE email = :email")
    suspend fun deleteTokenByEmail(email: String)
}
