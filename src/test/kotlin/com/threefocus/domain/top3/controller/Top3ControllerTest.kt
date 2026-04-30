package com.threefocus.domain.top3.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.top3.dto.SetTop3Request
import com.threefocus.domain.top3.dto.Top3Response
import com.threefocus.domain.top3.service.Top3Service
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

@WebMvcTest(Top3Controller::class)
@Import(SecurityConfig::class)
class Top3ControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var top3Service: Top3Service
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val today = LocalDate.of(2026, 4, 30)
    private val top3Response = listOf(
        Top3Response(id = 1L, todoId = 1L, date = today, orderIndex = 1),
        Top3Response(id = 2L, todoId = 2L, date = today, orderIndex = 2),
    )

    @Test
    @WithMockUser(username = "10")
    fun `POST top3 - 성공 시 200 반환`() {
        given(top3Service.setTop3(eq(10L), any())).willReturn(top3Response)

        mockMvc.post("/api/top3") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SetTop3Request(listOf(1L, 2L), today))
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].orderIndex") { value(1) }
            jsonPath("$[1].orderIndex") { value(2) }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `POST top3 - todoIds 비어있으면 400 반환`() {
        mockMvc.post("/api/top3") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SetTop3Request(emptyList(), today))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `POST top3 - 날짜 누락 시 400 반환`() {
        mockMvc.post("/api/top3") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"todoIds": [1, 2]}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST top3 - 인증 없으면 401 반환`() {
        mockMvc.post("/api/top3") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SetTop3Request(listOf(1L), today))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `GET top3 - 날짜별 Top3 조회 성공`() {
        given(top3Service.getTop3(10L, today)).willReturn(top3Response)

        mockMvc.get("/api/top3") {
            param("date", "2026-04-30")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
        }
    }
}
