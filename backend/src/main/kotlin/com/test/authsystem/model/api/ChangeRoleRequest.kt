package com.test.authsystem.model.api

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Change user role request")
data class ChangeRoleRequest(
    @Schema(description = "New role for the user", example = "reviewer", required = true)
    @field:NotBlank(message = "Role can't be blank")
    var newRole : String)