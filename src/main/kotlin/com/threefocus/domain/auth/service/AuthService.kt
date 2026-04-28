package com.threefocus.domain.auth.service

import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.RefreshRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.dto.TokenResponse
import com.threefocus.domain.auth.entity.User
import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.domain.auth.repository.UserRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import com.threefocus.global.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userQueryRepository: UserQueryRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional
    fun signUp(request: SignUpRequest): TokenResponse {
        if (userQueryRepository.existsByEmail(request.email)) {
            throw ApiException(ErrorCode.DUPLICATE_EMAIL)
        }
        val user = userRepository.save(
            User(email = request.email, password = passwordEncoder.encode(request.password))
        )
        return issueTokens(user.id)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): TokenResponse {
        val user = userQueryRepository.findByEmail(request.email)
            ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ApiException(ErrorCode.INVALID_PASSWORD)
        }
        return issueTokens(user.id)
    }

    @Transactional(readOnly = true)
    fun refresh(request: RefreshRequest): TokenResponse {
        if (!jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            throw ApiException(ErrorCode.INVALID_TOKEN)
        }
        val userId = runCatching { jwtTokenProvider.getUserId(request.refreshToken) }
            .getOrElse { throw ApiException(ErrorCode.INVALID_TOKEN) }
        return issueTokens(userId)
    }

    private fun issueTokens(userId: Long) = TokenResponse(
        accessToken = jwtTokenProvider.generateAccessToken(userId),
        refreshToken = jwtTokenProvider.generateRefreshToken(userId),
    )
}
