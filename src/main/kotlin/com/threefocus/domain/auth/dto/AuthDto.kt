package com.threefocus.domain.auth.dto

import com.threefocus.domain.auth.entity.Gender
import com.threefocus.domain.term.entity.TermType
import jakarta.validation.constraints.*
import java.time.LocalDate

data class SignUpRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank @field:Size(max = 50) val name: String,
    @field:NotBlank val phone: String,
    @field:NotNull val gender: Gender,
    @field:NotNull @field:Past val birthday: LocalDate,
    @field:NotEmpty val termAgreements: List<TermAgreementRequest>,
)

data class TermAgreementRequest(
    @field:NotNull val termType: TermType,
    @field:NotNull val agreed: Boolean,
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
