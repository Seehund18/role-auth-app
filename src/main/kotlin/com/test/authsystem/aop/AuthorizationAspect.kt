package com.test.authsystem.aop

import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.exception.AuthException
import com.test.authsystem.exception.NotEnoughPermissionsException
import com.test.authsystem.service.JwtTokenProvider
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
class AuthorizationAspect(
    val rolesRepo: RolesRepository,
    val jwtTokenProvider: JwtTokenProvider,
    val log: KLogger = KotlinLogging.logger {}
) {

    private val bearerTokenRegex = Regex("Bearer (.*)")

    @Before("@annotation(authorized)")
    @Throws(Throwable::class)
    fun authorizeRequest(joinPoint: JoinPoint, authorized: Authorized) {
        log.info { "Authorizing request" }
        val minRequiredRole = authorized.minRole

        val jwtToken = extractJwtTokenFromHeader();
        val jwtRole = jwtTokenProvider.parseJwt(jwtToken)
            .body[AuthClaims.ROLE.claimName]
            .toString()

        val minRequiredRoleEntity = rolesRepo.findByNameIgnoreCase(minRequiredRole.name)
        val jwtRoleCorrect = rolesRepo.findByPriorityValueLessThanEqual(minRequiredRoleEntity.priorityValue)
            .map { roleEntity -> roleEntity.name.lowercase() }
            .contains(jwtRole.lowercase())

        if (!jwtRoleCorrect) {
            throw NotEnoughPermissionsException("User's permissions isn't enough to access the endpoint")
        }
    }

    private fun extractJwtTokenFromHeader(): String {
        val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()
        var header: String = ""
        if (attributes is ServletRequestAttributes) {
            header = attributes.request.getHeader(HttpHeaders.AUTHORIZATION)
                ?: throw AuthException("No authorization header in the request")
        }
        return getTokenFromHeader(header) ?: throw AuthException("No authorization token in the request")
    }

    private fun getTokenFromHeader(header: String): String? {
        return bearerTokenRegex.find(header)?.groups?.get(1)?.value.toString()
    }
}