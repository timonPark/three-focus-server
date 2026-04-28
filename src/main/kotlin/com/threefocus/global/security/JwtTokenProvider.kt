package com.threefocus.global.security

import io.jsonwebtoken.Claims
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

    fun generateAccessToken(userId: Long): String = generateToken(userId, accessTokenExpiration, TOKEN_TYPE_ACCESS)

    fun generateRefreshToken(userId: Long): String = generateToken(userId, refreshTokenExpiration, TOKEN_TYPE_REFRESH)

    private fun generateToken(userId: Long, expiration: Long, type: String): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_TYPE, type)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()

    fun getUserId(token: String): Long = parseClaims(token).subject.toLong()

    fun isValid(token: String): Boolean = runCatching { parseClaims(token); true }.getOrDefault(false)

    fun isRefreshToken(token: String): Boolean =
        runCatching { parseClaims(token)[CLAIM_TYPE] == TOKEN_TYPE_REFRESH }.getOrDefault(false)

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    companion object {
        private const val CLAIM_TYPE = "type"
        private const val TOKEN_TYPE_ACCESS = "access"
        private const val TOKEN_TYPE_REFRESH = "refresh"
    }
}
