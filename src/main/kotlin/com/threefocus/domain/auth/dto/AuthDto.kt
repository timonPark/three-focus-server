package com.threefocus.domain.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
