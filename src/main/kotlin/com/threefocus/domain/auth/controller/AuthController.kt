package com.threefocus.domain.auth.controller

import com.threefocus.domain.auth.dto.CompleteProfileRequest
import com.threefocus.domain.auth.dto.GoogleLoginRequest
import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.RefreshRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.dto.TokenResponse
import com.threefocus.domain.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @Operation(
        summary = "회원가입",
        responses = [ApiResponse(
            responseCode = "201",
            content = [Content(schema = Schema(implementation = TokenResponse::class))],
        )],
    )
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): TokenResponse =
        authService.signUp(request)

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse =
        authService.login(request)

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): TokenResponse =
        authService.refresh(request)

    @Operation(summary = "Google OAuth 로그인/회원가입")
    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): TokenResponse =
        authService.googleLogin(request)

    @Operation(summary = "소셜 가입 후 추가 정보 입력")
    @PostMapping("/complete-profile")
    fun completeProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CompleteProfileRequest,
    ): TokenResponse =
        authService.completeProfile(userDetails.username.toLong(), request)
}
