package com.threefocus.domain.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.dto.TermAgreementRequest
import com.threefocus.domain.auth.dto.TokenResponse
import com.threefocus.domain.auth.entity.Gender
import com.threefocus.domain.auth.service.AuthService
import com.threefocus.domain.term.entity.TermType
import com.threefocus.global.config.SecurityConfig
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
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
import java.time.LocalDate

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var authService: AuthService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val tokens = TokenResponse("access-token", "refresh-token")

    private fun validSignUpRequest(
        email: String = "test@example.com",
        password: String = "password123",
        termAgreements: List<TermAgreementRequest> = listOf(
            TermAgreementRequest(TermType.SERVICE_TERMS, true),
            TermAgreementRequest(TermType.PRIVACY_POLICY, true),
            TermAgreementRequest(TermType.MARKETING, false),
        ),
    ) = SignUpRequest(
        email = email,
        password = password,
        name = "홍길동",
        phone = "010-1234-5678",
        gender = Gender.MALE,
        birthday = LocalDate.of(1990, 1, 1),
        termAgreements = termAgreements,
    )

    @Test
    fun `POST sign-up - 성공 시 201 반환`() {
        val request = validSignUpRequest()
        given(authService.signUp(request)).willReturn(tokens)

        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.accessToken") { value("access-token") }
        }
    }

    @Test
    fun `POST sign-up - 이메일 형식 오류 시 400 반환`() {
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validSignUpRequest(email = "not-an-email"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST sign-up - 비밀번호 8자 미만 시 400 반환`() {
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validSignUpRequest(password = "short"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST sign-up - 이메일 중복 시 409 반환`() {
        val request = validSignUpRequest()
        given(authService.signUp(request)).willThrow(ApiException(ErrorCode.DUPLICATE_EMAIL))

        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `POST sign-up - 필수 약관 미동의 시 400 반환`() {
        val request = validSignUpRequest(
            termAgreements = listOf(
                TermAgreementRequest(TermType.SERVICE_TERMS, true),
                TermAgreementRequest(TermType.PRIVACY_POLICY, false),
            )
        )
        given(authService.signUp(request)).willThrow(ApiException(ErrorCode.REQUIRED_TERMS_NOT_AGREED))

        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
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
