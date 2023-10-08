package com.test.authsystem.controller.v0

import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.SignInException
import com.test.authsystem.model.api.UserResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KLogger
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class ExceptionControllerAdvice(val log: KLogger = KotlinLogging.logger {}) {

    private val failedStatus: String = "FAILED"

    @ResponseBody
    @ResponseStatus(value= HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicateException(req: HttpServletRequest, ex: DuplicateException): UserResponse {
        log.error("Duplication Error: ", ex)
        return UserResponse(status = failedStatus, description = ex.message)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(req: HttpServletRequest, ex: MethodArgumentNotValidException): UserResponse {
        log.error("Validation Error: ", ex)

        val fieldErrorMsg = ex.fieldErrors.map { fieldError -> fieldError.defaultMessage }
            .sortedBy { str -> str }
            .joinToString(", ")

        return UserResponse(status = failedStatus, description = fieldErrorMsg)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadableException(req: HttpServletRequest, ex: HttpMessageNotReadableException): UserResponse {
        log.error("Validation Error: ", ex)

        return UserResponse(status = failedStatus, description = "Error with message format")
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SignInException::class)
    fun handleUserNotFoundException(req: HttpServletRequest, ex: SignInException): UserResponse {
        log.error("Error: ", ex)

        return UserResponse(status = failedStatus, description = ex.message)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleRuntimeException(req: HttpServletRequest, ex: Exception): UserResponse {
        log.error("Error: ", ex)

        return UserResponse(status = failedStatus, description = "Internal system error")
    }


}