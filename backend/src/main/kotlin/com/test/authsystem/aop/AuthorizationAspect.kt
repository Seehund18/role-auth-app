package com.test.authsystem.aop

import com.test.authsystem.exception.AuthException
import com.test.authsystem.service.AuthService
import com.test.authsystem.util.extractJwtTokenFromHeader
import mu.KLogger
import mu.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class AuthorizationAspect(val authService: AuthService) {

    private val log: KLogger = KotlinLogging.logger {}

    @Before("@annotation(authorized)")
    @Throws(Throwable::class)
    fun authorizeRequest(joinPoint: JoinPoint, authorized: Authorized) {
        log.info { "Authorizing request" }
        val minRequiredRole = authorized.minRole

        val jwtToken = extractJwtTokenFromHeader(getAuthHeader())
        authService.authorizeRequest(jwtToken, minRequiredRole)
    }

    private fun getAuthHeader(): String {
        val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()
        var authHeader: String? = null
        if (attributes is ServletRequestAttributes) {
            authHeader = attributes.request.getHeader(HttpHeaders.AUTHORIZATION)
        }
        return authHeader ?: throw AuthException("No authorization header in the request")
    }

}