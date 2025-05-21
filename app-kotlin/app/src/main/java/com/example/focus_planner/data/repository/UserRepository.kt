package com.example.focus_planner.data.repository

import android.content.Context
import com.example.focus_planner.data.local.AppDatabase
import com.example.focus_planner.data.model.UserToken
import com.example.focus_planner.data.local.UserTokenDao
import com.example.focus_planner.data.model.User
import com.example.focus_planner.model.LoginRequest
import com.example.focus_planner.model.RegisterRequest
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.network.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import javax.inject.Inject

    class UserRepository @Inject constructor(
        @ApplicationContext private val context: Context,
        private val apiService: ApiService = RetrofitInstance.api
    ) {
        private val userTokenDao: UserTokenDao = AppDatabase.getDatabase(context).userTokenDao()

        fun getUserTokenDao(): UserTokenDao {
            return userTokenDao
        }
        suspend fun getToken(email: String): UserToken? = userTokenDao.getTokenByEmail(email)

        suspend fun insertToken(userToken: UserToken) = userTokenDao.insert(userToken)

        suspend fun deleteTokenByEmail(email: String) = userTokenDao.deleteTokenByEmail(email)

        suspend fun getUserFromToken(token: String): UserToken? = userTokenDao.getTokenByEmail(token)

        suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))
        suspend fun register(name: String, email: String, password: String) = apiService.register(
            RegisterRequest(name, email, password)
        )
        suspend fun updateUserById(userId: String, token: String, user: User): Response<User> {
            return apiService.updateUserById(userId, "Bearer $token", user)
        }

        suspend fun getUserById(userId: Long, token: String): Response<User> {
            return apiService.getUserById(userId, "Bearer $token")
        }

        suspend fun deleteAccount(authHeader: String) {
            apiService.deleteMyAccount(authHeader)
        }
    }


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideApiService(): ApiService {
        return RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
    }
}
