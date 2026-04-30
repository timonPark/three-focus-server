package com.threefocus.domain.auth.service

import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.RefreshRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.entity.User
import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.domain.auth.repository.UserRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import com.threefocus.global.security.JwtTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var userQueryRepository: UserQueryRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var jwtTokenProvider: JwtTokenProvider

    @InjectMocks private lateinit var authService: AuthService

    private val savedUser = User(id = 1L, email = "test@example.com", password = "encoded_pw")

    @Test
    fun `signUp - 성공 시 토큰 반환`() {
        given(userQueryRepository.existsByEmail("test@example.com")).willReturn(false)
        given(passwordEncoder.encode("password123")).willReturn("encoded_pw")
        given(userRepository.save(any())).willReturn(savedUser)
        given(jwtTokenProvider.generateAccessToken(1L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token")

        val result = authService.signUp(SignUpRequest("test@example.com", "password123"))

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
    }

    @Test
    fun `signUp - 이메일 중복 시 DUPLICATE_EMAIL 예외`() {
        given(userQueryRepository.existsByEmail("test@example.com")).willReturn(true)

        val ex = assertThrows<ApiException> {
            authService.signUp(SignUpRequest("test@example.com", "password123"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.DUPLICATE_EMAIL)
    }

    @Test
    fun `login - 성공 시 토큰 반환`() {
        given(userQueryRepository.findByEmail("test@example.com")).willReturn(savedUser)
        given(passwordEncoder.matches("password123", "encoded_pw")).willReturn(true)
        given(jwtTokenProvider.generateAccessToken(1L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token")

        val result = authService.login(LoginRequest("test@example.com", "password123"))

        assertThat(result.accessToken).isEqualTo("access-token")
    }

    @Test
    fun `login - 이메일 없으면 UNAUTHORIZED 예외`() {
        given(userQueryRepository.findByEmail("test@example.com")).willReturn(null)

        val ex = assertThrows<ApiException> {
            authService.login(LoginRequest("test@example.com", "password123"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED)
    }

    @Test
    fun `login - 비밀번호 불일치 시 INVALID_PASSWORD 예외`() {
        given(userQueryRepository.findByEmail("test@example.com")).willReturn(savedUser)
        given(passwordEncoder.matches("wrong_pw", "encoded_pw")).willReturn(false)

        val ex = assertThrows<ApiException> {
            authService.login(LoginRequest("test@example.com", "wrong_pw"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INVALID_PASSWORD)
    }

    @Test
    fun `refresh - 유효한 리프레시 토큰으로 새 토큰 반환`() {
        given(jwtTokenProvider.isRefreshToken("valid-refresh")).willReturn(true)
        given(jwtTokenProvider.getUserId("valid-refresh")).willReturn(1L)
        given(jwtTokenProvider.generateAccessToken(1L)).willReturn("new-access")
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("new-refresh")

        val result = authService.refresh(RefreshRequest("valid-refresh"))

        assertThat(result.accessToken).isEqualTo("new-access")
    }

    @Test
    fun `refresh - 액세스 토큰 사용 시 INVALID_TOKEN 예외`() {
        given(jwtTokenProvider.isRefreshToken("access-token")).willReturn(false)

        val ex = assertThrows<ApiException> {
            authService.refresh(RefreshRequest("access-token"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INVALID_TOKEN)
    }
}
