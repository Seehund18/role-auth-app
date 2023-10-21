package com.syakim.authsystemfront

import io.kvision.core.StringPair
import io.kvision.rest.RestClient
import io.kvision.rest.call
import io.kvision.rest.post
import com.syakim.authsystemfront.model.AuthResponse
import com.syakim.authsystemfront.model.User
import kotlinx.coroutines.await

object Api {

    const val API_URL = "http://localhost:8080/v0"

    private val restClient = RestClient()

    private fun authRequest(): List<StringPair> {
        return RoleAuthManager.getJwtToken()?.let {
            listOf("Authorization" to "Bearer $it")
        } ?: emptyList()
    }

    suspend fun login(username: String, password: String): Pair<User, AuthResponse> {
        val user = User(
            login = username,
            password = password
        )
        val response = restClient.post<AuthResponse, User>(
            "$API_URL/users/auth",
            user
        ).await()

        return user to response
    }

    suspend fun getUserInfo(username: String?): User {
        return restClient.call<User>(
            "$API_URL/users/$username"
        ) { headers = ::authRequest }.await()
    }
}
