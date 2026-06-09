package com.example.queues.api

import com.example.queues.dto.AuthDto
import com.example.queues.dto.TokenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {
    @POST("/api/login")
    suspend fun login(
        @Body dto: AuthDto
    ): Response<TokenDto>
    @POST("/api/register")
    suspend fun register(
        @Body dto: AuthDto
    ): Response<TokenDto>
}
