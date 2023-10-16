package com.test.authsystem.model.api

import jakarta.validation.constraints.NotBlank

data class ChangeRoleRequest(
    @field:NotBlank(message = "Role can't be blank")
    var newRole : String)