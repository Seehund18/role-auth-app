package com.test.authsystem.controller.v0

import com.test.authsystem.aop.Authorized
import com.test.authsystem.constants.SystemResponseStatus

import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.model.api.StatusResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v0/roles")
class RolesController {

    @Authorized(minRole = SystemRoles.ADMIN)
    @GetMapping("/admin")
    fun adminFun() : StatusResponse {
        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Response from admin endpoint")
    }

    @Authorized(minRole = SystemRoles.REVIEWER)
    @GetMapping("/reviewer")
    fun reviewerFun() : StatusResponse {
        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Response from reviewer endpoint")
    }

    @Authorized(minRole = SystemRoles.USER)
    @GetMapping("/user")
    fun userFun() : StatusResponse {
        return StatusResponse(status = SystemResponseStatus.SUCCESS.name, description = "Response from user endpoint")
    }

}