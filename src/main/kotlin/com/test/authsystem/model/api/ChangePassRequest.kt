package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword

data class ChangePassRequest(
    val oldPass : CharArray,
    @field:NotBlankPassword(message = "New password must not be blank")
    val newPass : CharArray
)