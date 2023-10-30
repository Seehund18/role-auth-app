package com.test.authsystem.aop

import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.exception.AuthException
import com.test.authsystem.exception.UsersDontMatchException
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
        val claimsMap = authService.authorizeRequest(jwtToken, minRequiredRole)

        if (authorized.verifyJwtAndRequestLogin) {
            verifyJwtAndRequestLogin(joinPoint, claimsMap)
        }
    }

    private fun getAuthHeader(): String {
        val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()
        var authHeader: String? = null
        if (attributes is ServletRequestAttributes) {
            authHeader = attributes.request.getHeader(HttpHeaders.AUTHORIZATION)
        }
        return authHeader ?: throw AuthException("No authorization header in the request")
    }

    private fun verifyJwtAndRequestLogin(joinPoint: JoinPoint, claimsMap: Map<String, String>) {
        val requestLogin = joinPoint.args[0]
        if (requestLogin !is String ||
            claimsMap[AuthClaims.LOGIN.claimName]?.lowercase() != requestLogin.lowercase()
        ) {
            throw UsersDontMatchException("Other users can't be changed or queried")
        }
    }
}
