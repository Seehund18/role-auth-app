package com.syakim.authsystemfront.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String? = null,
    val token: String? = null,
    val login: String? = null,
    val password: String? = null,
    val birthday: String? = null,
    val role: String? = null,
    val endpoints: List<Endpoint> = emptyList()
)
@Serializable
data class Endpoint(
    val url: String?,
    val description: String?
)
