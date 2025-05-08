package com.example.focus_planner.network

import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.UpdateUserRequest
import com.example.focus_planner.data.model.User
import com.example.focus_planner.data.model.UserResponse
import com.example.focus_planner.model.LoginRequest
import com.example.focus_planner.model.LoginResponse
import com.example.focus_planner.model.RegisterRequest
import com.example.focus_planner.model.RegisterResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Definimos las solicitudes HTTP para login y registro
//data class LoginRequest(val email: String, val password: String)
//data class RegisterRequest(val name: String, val email: String, val password: String)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>


    @PUT("api/users/{id}")
    suspend fun updateUserById(
        @Path("id") userId: String,
        @Header("Authorization") token: String,
        @Body updatedUser: User,
    ): Response<Void>

    @POST("api/users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordChangeBody: Map<String, String>
    ): Response<Void>

//    @GET("api/users/{id}")
//    suspend fun getUserById(
//        @Path("id") userId: Long,
//        @Header("Authorization") token: String
//    ): Response<User>
////
//    @PUT("api/users/{id}")
//    suspend fun updateUserById(
//        @Path("id") userId: Long,
//        @Header("Authorization") token: String,
//        @Body updatedUser: User
//    ): Response<User>

    @PUT("api/users/{id}")
    suspend fun updateUserProfile(
        @Path("id") userId: Long,
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") authorizationHeader: String): Response<User>
//
//    @GET("api/user/{id}")
//    suspend fun getUserProfileID(
//        @Path("id") userId: String,
//        @Header("Authorization") token: String
//    ): Response<User>

    //refrescar el token
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body body: RequestBody): Response<LoginResponse>


    @GET("/api/tasks")
    suspend fun getTasks(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("completed") completed: Boolean? = null,
        @Header("Authorization") token: String
    ): Response<List<Task>>

    @GET("/api/tasks/{id}")
    suspend fun getTaskDetails(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Task>

//    @GET("api/tasks")
//    suspend fun getTasks(
//        @Query("page") page: Int,
//        @Query("size") size: Int,
////        @Query("completed") completed: Boolean? = null,
////        @Query("search") search: String? = null,
////        @Query("status") status: String? = null,
////        @Query("priority") priority: String? = null,
//        @Header("Authorization") authorization: String
//    ): Response<List<Task>>

    @GET("api/tasks/filter")
    suspend fun getFilteredTasks(
        @Query("title") title: String? = null,
        @Query("completed") completed: Boolean? = null,
        @Query("dueDate") dueDate: String? = null,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Header("Authorization") token: String
    ): Response<List<Task>>


    @DELETE("/api/tasks/{id}")
    suspend fun deleteTaskById(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Void>

    @POST("/api/tasks")
    suspend fun createTask(
        @Body task: Task,
        @Header("Authorization") token: String
    ): Response<Task>

    @GET("/api/tasks/{id}")
    suspend fun getTaskById(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Task>

    @PUT("/api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Header("Authorization") token: String,
        @Body task: Task
    ): Response<Task>



}
//Tasks
// Aquí puedes agregar las funciones para las tareas, como obtener la lista de tareas, crear una nueva tarea, etc.
// Por ejemplo:
//interface TaskApi {
//
//    @GET("/api/tasks")
//    suspend fun getTasks(
//        @Query("page") page: Int,
//        @Query("size") size: Int,
//        @Query("completed") completed: Boolean? = null,
//        @Header("Authorization") token: String
//    ): Response<List<Task>>
//
//    @GET("/api/tasks/{id}")
//    suspend fun getTaskDetails(
//        @Path("id") id: Long,
//        @Header("Authorization") token: String
//    ): Response<Task>
//
//    @GET("api/tasks")
//    suspend fun getTasks(
//        @Query("page") page: Int,
//        @Query("size") size: Int,
//        @Query("completed") completed: Boolean? = null,
//        @Query("search") search: String? = null,
//        @Query("status") status: String? = null,
//        @Query("priority") priority: String? = null,
//        @Header("Authorization") authorization: String
//    ): Response<List<Task>>
//
//
//}