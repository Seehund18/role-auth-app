package com.test.authsystem.model.api

data class ChangePassRequest(
    val oldPass : CharArray,
    val newPass : CharArray
)