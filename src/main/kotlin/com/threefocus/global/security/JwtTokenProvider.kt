package com.threefocus.global.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateAccessToken(userId: Long): String = generateToken(userId, accessTokenExpiration)

    fun generateRefreshToken(userId: Long): String = generateToken(userId, refreshTokenExpiration)

    private fun generateToken(userId: Long, expiration: Long): String =
        Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()

    fun getUserId(token: String): Long =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject.toLong()

    fun isValid(token: String): Boolean = runCatching { getUserId(token); true }.getOrDefault(false)
}
