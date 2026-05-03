package com.threefocus.domain.auth.service

import com.threefocus.domain.auth.entity.AuthProvider
import com.threefocus.domain.auth.entity.User
import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock private lateinit var userQueryRepository: UserQueryRepository

    @InjectMocks private lateinit var userService: UserService

    private val user = User(
        id = 1L,
        email = "test@example.com",
        name = "홍길동",
        provider = AuthProvider.LOCAL,
        isProfileComplete = true,
    )

    @Test
    fun `getMe - 성공 시 UserResponse 반환`() {
        given(userQueryRepository.findById(1L)).willReturn(user)

        val result = userService.getMe(1L)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("홍길동")
        assertThat(result.email).isEqualTo("test@example.com")
    }

    @Test
    fun `getMe - 존재하지 않는 사용자 UNAUTHORIZED 예외`() {
        given(userQueryRepository.findById(99L)).willReturn(null)

        val ex = assertThrows<ApiException> { userService.getMe(99L) }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED)
    }
}
