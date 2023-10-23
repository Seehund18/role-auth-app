package com.syakim.authsystemfront

import io.kvision.core.StringPair
import io.kvision.rest.RestClient
import io.kvision.rest.call
import io.kvision.rest.post
import com.syakim.authsystemfront.model.AuthResponse
import com.syakim.authsystemfront.model.StatusResponse
import com.syakim.authsystemfront.model.User
import kotlinx.coroutines.await

object Api {

    const val BASE_URL = "http://localhost:8080"
    const val AUTH_ENDPOINT = "/v0/auth"
    const val GET_USER_INFO_ENDPOINT = "/v0/users"

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
            BASE_URL + AUTH_ENDPOINT,
            user
        ).await()

        return user to response
    }

    suspend fun getUserInfo(username: String?): User {
        return restClient.call<User>(
            "${BASE_URL + GET_USER_INFO_ENDPOINT}/$username"
        ) { headers = ::authRequest }.await()
    }

    suspend fun sendRoleRequest(roleEndpoint: String?): StatusResponse {
        return restClient.call<StatusResponse>(
            BASE_URL + roleEndpoint
        ) { headers = ::authRequest }.await()
    }
}
