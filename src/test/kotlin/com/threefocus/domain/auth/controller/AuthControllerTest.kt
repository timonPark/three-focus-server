package com.threefocus.domain.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.dto.TokenResponse
import com.threefocus.domain.auth.service.AuthService
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import com.threefocus.global.config.SecurityConfig
import com.threefocus.global.security.JwtTokenProvider
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var authService: AuthService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val tokens = TokenResponse("access-token", "refresh-token")

    @Test
    fun `POST sign-up - 성공 시 201 반환`() {
        given(authService.signUp(SignUpRequest("test@example.com", "password123"))).willReturn(tokens)

        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SignUpRequest("test@example.com", "password123"))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.accessToken") { value("access-token") }
        }
    }

    @Test
    fun `POST sign-up - 이메일 형식 오류 시 400 반환`() {
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SignUpRequest("not-an-email", "password123"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST sign-up - 비밀번호 8자 미만 시 400 반환`() {
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SignUpRequest("test@example.com", "short"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST sign-up - 이메일 중복 시 409 반환`() {
        given(authService.signUp(SignUpRequest("test@example.com", "password123")))
            .willThrow(ApiException(ErrorCode.DUPLICATE_EMAIL))

        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SignUpRequest("test@example.com", "password123"))
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `POST login - 성공 시 200 반환`() {
        given(authService.login(LoginRequest("test@example.com", "password123"))).willReturn(tokens)

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("test@example.com", "password123"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("access-token") }
            jsonPath("$.refreshToken") { value("refresh-token") }
        }
    }

    @Test
    fun `POST login - 잘못된 비밀번호 시 401 반환`() {
        given(authService.login(LoginRequest("test@example.com", "wrong")))
            .willThrow(ApiException(ErrorCode.INVALID_PASSWORD))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("test@example.com", "wrong"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
