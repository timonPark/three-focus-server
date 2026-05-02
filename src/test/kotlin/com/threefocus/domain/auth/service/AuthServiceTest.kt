package com.threefocus.domain.auth.service

import com.threefocus.domain.auth.dto.CompleteProfileRequest
import com.threefocus.domain.auth.dto.GoogleLoginRequest
import com.threefocus.domain.auth.dto.LoginRequest
import com.threefocus.domain.auth.dto.RefreshRequest
import com.threefocus.domain.auth.dto.SignUpRequest
import com.threefocus.domain.auth.dto.TermAgreementRequest
import com.threefocus.domain.auth.entity.AuthProvider
import com.threefocus.domain.auth.entity.Gender
import com.threefocus.domain.auth.entity.User
import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.domain.auth.repository.UserRepository
import com.threefocus.domain.term.entity.Term
import com.threefocus.domain.term.entity.TermType
import com.threefocus.domain.term.repository.TermRepository
import com.threefocus.domain.term.repository.UserTermRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import com.threefocus.global.security.GoogleTokenVerifier
import com.threefocus.global.security.GoogleUserInfo
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var userQueryRepository: UserQueryRepository
    @Mock private lateinit var termRepository: TermRepository
    @Mock private lateinit var userTermRepository: UserTermRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var jwtTokenProvider: JwtTokenProvider
    @Mock private lateinit var googleTokenVerifier: GoogleTokenVerifier

    @InjectMocks private lateinit var authService: AuthService

    private val savedUser = User(
        id = 1L,
        email = "test@example.com",
        password = "encoded_pw",
        name = "홍길동",
        phone = "010-1234-5678",
        gender = Gender.MALE,
        birthday = LocalDate.of(1990, 1, 1),
        provider = AuthProvider.LOCAL,
        isProfileComplete = true,
    )

    private val googleUser = User(
        id = 2L,
        email = "google@example.com",
        name = "구글 사용자",
        provider = AuthProvider.GOOGLE,
        providerId = "google-sub-123",
        isProfileComplete = false,
    )

    private val requiredTerms = listOf(
        Term(id = 1L, type = TermType.SERVICE_TERMS, title = "서비스 이용약관 동의", isRequired = true, version = "1.0"),
        Term(id = 2L, type = TermType.PRIVACY_POLICY, title = "개인정보 처리방침 동의", isRequired = true, version = "1.0"),
    )
    private val allTerms = requiredTerms + listOf(
        Term(id = 3L, type = TermType.MARKETING, title = "마케팅 정보 수신 동의", isRequired = false, version = "1.0"),
    )

    private fun signUpRequest(
        termAgreements: List<TermAgreementRequest> = listOf(
            TermAgreementRequest(TermType.SERVICE_TERMS, true),
            TermAgreementRequest(TermType.PRIVACY_POLICY, true),
            TermAgreementRequest(TermType.MARKETING, false),
        ),
    ) = SignUpRequest(
        email = "test@example.com",
        password = "password123",
        name = "홍길동",
        phone = "010-1234-5678",
        gender = Gender.MALE,
        birthday = LocalDate.of(1990, 1, 1),
        termAgreements = termAgreements,
    )

    private fun completeProfileRequest() = CompleteProfileRequest(
        phone = "010-9999-8888",
        gender = Gender.FEMALE,
        birthday = LocalDate.of(1995, 6, 15),
        termAgreements = listOf(
            TermAgreementRequest(TermType.SERVICE_TERMS, true),
            TermAgreementRequest(TermType.PRIVACY_POLICY, true),
            TermAgreementRequest(TermType.MARKETING, false),
        ),
    )

    @Test
    fun `signUp - 성공 시 토큰 반환`() {
        given(userQueryRepository.existsByEmail("test@example.com")).willReturn(false)
        given(termRepository.findAllByIsRequired(true)).willReturn(requiredTerms)
        given(termRepository.findAll()).willReturn(allTerms)
        given(passwordEncoder.encode("password123")).willReturn("encoded_pw")
        given(userRepository.save(any())).willReturn(savedUser)
        given(jwtTokenProvider.generateAccessToken(1L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token")

        val result = authService.signUp(signUpRequest())

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.isProfileComplete).isTrue()
    }

    @Test
    fun `signUp - 이메일 중복 시 DUPLICATE_EMAIL 예외`() {
        given(userQueryRepository.existsByEmail("test@example.com")).willReturn(true)

        val ex = assertThrows<ApiException> { authService.signUp(signUpRequest()) }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.DUPLICATE_EMAIL)
    }

    @Test
    fun `signUp - 필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외`() {
        given(userQueryRepository.existsByEmail("test@example.com")).willReturn(false)
        given(termRepository.findAllByIsRequired(true)).willReturn(requiredTerms)

        val ex = assertThrows<ApiException> {
            authService.signUp(signUpRequest(
                termAgreements = listOf(
                    TermAgreementRequest(TermType.SERVICE_TERMS, true),
                    TermAgreementRequest(TermType.PRIVACY_POLICY, false),
                )
            ))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED)
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
        given(userQueryRepository.findById(1L)).willReturn(savedUser)
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

    @Test
    fun `googleLogin - 기존 Google 계정 로그인 성공`() {
        val googleInfo = GoogleUserInfo("google-sub-123", "google@example.com", "구글 사용자", true)
        given(googleTokenVerifier.verify("google-id-token")).willReturn(googleInfo)
        given(userQueryRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-123")).willReturn(googleUser)
        given(jwtTokenProvider.generateAccessToken(2L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(2L)).willReturn("refresh-token")

        val result = authService.googleLogin(GoogleLoginRequest("google-id-token"))

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.isProfileComplete).isFalse()
    }

    @Test
    fun `googleLogin - 신규 Google 계정 가입 후 isProfileComplete false 반환`() {
        val googleInfo = GoogleUserInfo("new-sub-456", "new@example.com", "신규 사용자", true)
        val newGoogleUser = User(id = 3L, email = "new@example.com", name = "신규 사용자", provider = AuthProvider.GOOGLE, providerId = "new-sub-456", isProfileComplete = false)
        given(googleTokenVerifier.verify("new-id-token")).willReturn(googleInfo)
        given(userQueryRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "new-sub-456")).willReturn(null)
        given(userQueryRepository.findByEmail("new@example.com")).willReturn(null)
        given(userRepository.save(any())).willReturn(newGoogleUser)
        given(jwtTokenProvider.generateAccessToken(3L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(3L)).willReturn("refresh-token")

        val result = authService.googleLogin(GoogleLoginRequest("new-id-token"))

        assertThat(result.isProfileComplete).isFalse()
    }

    @Test
    fun `googleLogin - 동일 이메일이 LOCAL 계정으로 가입되어 있으면 EMAIL_ALREADY_REGISTERED 예외`() {
        val googleInfo = GoogleUserInfo("sub-999", "test@example.com", "홍길동", true)
        given(googleTokenVerifier.verify("id-token")).willReturn(googleInfo)
        given(userQueryRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "sub-999")).willReturn(null)
        given(userQueryRepository.findByEmail("test@example.com")).willReturn(savedUser)

        val ex = assertThrows<ApiException> {
            authService.googleLogin(GoogleLoginRequest("id-token"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED)
    }

    @Test
    fun `completeProfile - 성공 시 isProfileComplete true 반환`() {
        given(userQueryRepository.findById(2L)).willReturn(googleUser)
        given(termRepository.findAllByIsRequired(true)).willReturn(requiredTerms)
        given(termRepository.findAll()).willReturn(allTerms)
        given(userRepository.save(any())).willReturn(googleUser)
        given(jwtTokenProvider.generateAccessToken(2L)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(2L)).willReturn("refresh-token")

        val result = authService.completeProfile(2L, completeProfileRequest())

        assertThat(result.isProfileComplete).isTrue()
    }

    @Test
    fun `completeProfile - 이미 완성된 프로필이면 PROFILE_ALREADY_COMPLETE 예외`() {
        given(userQueryRepository.findById(1L)).willReturn(savedUser)

        val ex = assertThrows<ApiException> {
            authService.completeProfile(1L, completeProfileRequest())
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.PROFILE_ALREADY_COMPLETE)
    }
}
