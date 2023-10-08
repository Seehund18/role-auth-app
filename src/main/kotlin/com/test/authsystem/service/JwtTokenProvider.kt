package com.test.authsystem.service

import com.test.authsystem.config.props.JwtTokenProps
import com.test.authsystem.constants.AuthClaims
import com.test.authsystem.exception.JwtTokenException
import com.test.authsystem.model.db.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


@Service
class JwtTokenProvider(var tokenProps: JwtTokenProps) {

    fun generateJwtToken(userEntity: UserEntity): Pair<String, LocalDate> {
        val key = Keys.hmacShaKeyFor(tokenProps.secret.toByteArray())
        val expirationDate = getExpirationDate(tokenProps.lifespan)

        val jwt = Jwts.builder()
            .setExpiration(Date.from(expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setIssuer(tokenProps.issuer)
            .setAudience("auth-system")
            .signWith(key, SignatureAlgorithm.HS256)
            .setSubject(userEntity.id.toString())
            .claim(AuthClaims.EMAIL.claimName, userEntity.email)
            .claim(AuthClaims.LOGIN.claimName, userEntity.login)
            .claim(AuthClaims.ROLE.claimName, userEntity.role.name)
            .compact();

        return jwt to expirationDate
    }

    private fun getExpirationDate(tokenDuration: Duration): LocalDate {
        return LocalDate.now().plusDays(tokenDuration.toDays());
    }

    fun parseJwt(jwtToken: String): Jws<Claims> {
        val key = Keys.hmacShaKeyFor(tokenProps.secret.toByteArray())

        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwtToken)
        } catch (ex: Exception) {
            throw JwtTokenException("Malformed jwt token", ex)
        }
    }
}