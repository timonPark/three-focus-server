package com.threefocus.domain.share.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.dto.SharedScheduleResponse
import com.threefocus.domain.share.dto.ShareResponse
import com.threefocus.domain.share.service.ShareService
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import com.threefocus.global.config.SecurityConfig
import com.threefocus.global.security.JwtTokenProvider
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(ShareController::class)
@Import(SecurityConfig::class)
class ShareControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var shareService: ShareService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val today = LocalDate.of(2026, 4, 30)
    private val token = "test-token-uuid"

    @Test
    @WithMockUser(username = "10")
    fun `POST shares - 공유 링크 생성 성공 시 201 반환`() {
        val response = ShareResponse(id = 1L, shareToken = token, date = today, createdAt = LocalDateTime.now())
        given(shareService.createShare(eq(10L), any())).willReturn(response)

        mockMvc.post("/api/shares") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateShareRequest(today))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.shareToken") { value(token) }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `POST shares - 날짜 누락 시 400 반환`() {
        mockMvc.post("/api/shares") {
            contentType = MediaType.APPLICATION_JSON
            content = """{}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST shares - 인증 없으면 401 반환`() {
        mockMvc.post("/api/shares") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateShareRequest(today))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET shares-token - 인증 없이 공유 일정 조회 성공 시 200 반환`() {
        val response = SharedScheduleResponse(shareToken = token, date = today, top3 = emptyList())
        given(shareService.getShare(token)).willReturn(response)

        mockMvc.get("/api/shares/$token").andExpect {
            status { isOk() }
            jsonPath("$.shareToken") { value(token) }
            jsonPath("$.top3") { isArray() }
        }
    }

    @Test
    fun `GET shares-token - 존재하지 않는 토큰 조회 시 404 반환`() {
        given(shareService.getShare("invalid-token"))
            .willThrow(ApiException(ErrorCode.NOT_FOUND))

        mockMvc.get("/api/shares/invalid-token").andExpect {
            status { isNotFound() }
        }
    }
}
