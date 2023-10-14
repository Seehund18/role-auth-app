package com.test.authsystem.controller.v0

import com.test.authsystem.constants.SystemResponseStatus
import com.test.authsystem.exception.AuthException
import com.test.authsystem.exception.DuplicateException
import com.test.authsystem.exception.JwtTokenException
import com.test.authsystem.exception.NotEnoughPermissionsException
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.exception.UsersDontMatchException
import com.test.authsystem.model.api.StatusResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KLogger
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class ExceptionControllerAdvice(val log: KLogger = KotlinLogging.logger {}) {

    @ResponseBody
    @ResponseStatus(value= HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicateException(req: HttpServletRequest, ex: DuplicateException): StatusResponse {
        log.error("Duplication Error: ", ex)
        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = ex.message ?: "Data conflict")
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(req: HttpServletRequest, ex: MethodArgumentNotValidException): StatusResponse {
        log.error("Validation Error: ", ex)

        val fieldErrorMsg = ex.fieldErrors.map { fieldError -> fieldError.defaultMessage }
            .sortedBy { str -> str }
            .joinToString(", ")

        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = fieldErrorMsg)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadableException(req: HttpServletRequest, ex: HttpMessageNotReadableException): StatusResponse {
        log.error("Validation Error: ", ex)

        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = "Error with message format")
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PassDoesntMatchException::class, NoSuchElementException::class)
    fun handleUserNotFoundException(req: HttpServletRequest, ex: Exception): StatusResponse {
        log.error("Error: ", ex)

        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = ex.message)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthException::class, JwtTokenException::class, MissingRequestHeaderException::class)
    fun handleAuthExceptions(req: HttpServletRequest, ex: Exception): StatusResponse {
        log.error("Authorization exception: ", ex)
        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = ex.message)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.FORBIDDEN)
    @ExceptionHandler(NotEnoughPermissionsException::class, UsersDontMatchException::class)
    fun handleRuntimeException(req: HttpServletRequest, ex: Exception): StatusResponse {
        log.error("User doesn't have necessary permissions: ", ex)
        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = ex.message)
    }

    @ResponseBody
    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(req: HttpServletRequest, ex: Exception): StatusResponse {
        log.error("Error: ", ex)
        return StatusResponse(status = SystemResponseStatus.FAILED.name, description = "Internal system error")
    }

}