package com.threefocus.domain.auth.controller

import com.threefocus.domain.auth.dto.UserResponse
import com.threefocus.domain.auth.service.UserService
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
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(UserController::class)
@Import(SecurityConfig::class)
class UserControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc

    @MockBean private lateinit var userService: UserService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    @Test
    @WithMockUser(username = "1")
    fun `GET users-me - 성공 시 200 반환`() {
        given(userService.getMe(1L)).willReturn(UserResponse(id = 1L, name = "홍길동", email = "test@example.com"))

        mockMvc.get("/api/users/me").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("홍길동") }
            jsonPath("$.email") { value("test@example.com") }
        }
    }

    @Test
    fun `GET users-me - 인증 없으면 401 반환`() {
        mockMvc.get("/api/users/me").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(username = "99")
    fun `GET users-me - 존재하지 않는 사용자 401 반환`() {
        given(userService.getMe(99L)).willThrow(ApiException(ErrorCode.UNAUTHORIZED))

        mockMvc.get("/api/users/me").andExpect {
            status { isUnauthorized() }
        }
    }
}
