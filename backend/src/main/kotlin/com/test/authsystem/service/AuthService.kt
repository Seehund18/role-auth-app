package com.test.authsystem.service

import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.constants.SystemRoles
import com.test.authsystem.db.RolesRepository
import com.test.authsystem.db.UsersRepository
import com.test.authsystem.exception.JwtTokenException
import com.test.authsystem.exception.NoEntityWasFound
import com.test.authsystem.exception.NotEnoughPermissionsException
import com.test.authsystem.exception.PassDoesntMatchException
import com.test.authsystem.model.api.AuthRequest
import com.test.authsystem.model.db.UserEntity
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val rolesRepo: RolesRepository,
    private val usersRepo: UsersRepository,
    private val passHashingService: PassHashingService,
    private val jwtTokenHandler: JwtTokenHandler
) {

    private val log: KLogger = KotlinLogging.logger {}

    @Transactional
    fun signInUser(authRequest: AuthRequest): Pair<String, LocalDateTime> {
        val user =
            usersRepo.findByLoginIgnoreCase(authRequest.login)
                ?: throw NoEntityWasFound("User with given login doesn't exist")
        if (!checkUserPassword(authRequest.password, user)) {
            throw PassDoesntMatchException("Wrong password")
        }

        val jwtToken = jwtTokenHandler.generateJwtToken(user)

        log.info { "User ${user.login} has been signed in with token $jwtToken" }
        return jwtToken
    }

    private fun checkUserPassword(password: CharArray, user: UserEntity): Boolean {
        val generatedPass = passHashingService.generateHashedPassWithSalt(password, user.passwordEntity.salt)
        return user.passwordEntity.passwordHash.contentEquals(generatedPass)
    }

    @Transactional
    fun authorizeRequest(jwtToken: String, minRequiredRole: SystemRoles): Map<String, String> {
        val claimsMap = jwtTokenHandler.getAllClaimsFromToken(jwtToken)
        val jwtUser = claimsMap[AuthClaims.LOGIN.claimName] ?: throw JwtTokenException("Malformed jwt token")

        val userRole = usersRepo.findByLoginIgnoreCase(jwtUser)?.role?.name
            ?: throw RuntimeException("User with given login doesn't exist")
        val minRequiredRoleEntity = rolesRepo.findByNameIgnoreCase(minRequiredRole.name)
            ?: throw RuntimeException("$minRequiredRole wasn't found")

        val jwtRoleCorrect = rolesRepo.findByPriorityValueLessThanEqual(minRequiredRoleEntity.priorityValue)
            .map { roleEntity -> roleEntity.name.lowercase() }
            .contains(userRole.lowercase())
        if (!jwtRoleCorrect) {
            throw NotEnoughPermissionsException("User's permissions isn't enough to access the endpoint")
        }

        log.info { "User $jwtUser has been logged" }
        return claimsMap
    }

}