package com.test.authsystem.model.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Generic response with status")
data class StatusResponse(
    @Schema(example = "FAILED", required = true)
    val status: String,

    @Schema(example = "Error with message format", required = true, type = "String")
    val description: String?
)