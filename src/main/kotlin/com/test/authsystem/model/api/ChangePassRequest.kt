package com.test.authsystem.model.api

import com.test.authsystem.validation.NotBlankPassword
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Change password request")
data class ChangePassRequest(
    @Schema(description = "Old user password", example = "someOldPassword", required = true, type = "String")
    val oldPass : CharArray,

    @Schema(description = "New user password", example = "someNewPassword", required = true, type = "String")
    @field:NotBlankPassword(message = "New password must not be blank")
    val newPass : CharArray
)