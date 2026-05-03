package com.threefocus.domain.schedule.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.DailyScheduleItemResponse
import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.service.ScheduleService
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.time.LocalTime

@WebMvcTest(ScheduleController::class)
@Import(SecurityConfig::class)
class ScheduleControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var scheduleService: ScheduleService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val today = LocalDate.of(2026, 4, 30)

    @Test
    @WithMockUser(username = "10")
    fun `PUT schedules - 시간 배치 성공 시 200 반환`() {
        val response = ScheduleResponse(id = 1L, todoId = 1L, date = today, startTime = LocalTime.of(7, 0), endTime = null)
        given(scheduleService.assign(eq(10L), any())).willReturn(response)

        mockMvc.put("/api/schedules") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssignScheduleRequest(todoId = 1L, date = today, startTime = LocalTime.of(7, 0))
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.todoId") { value(1) }
            jsonPath("$.startTime") { value("07:00:00") }
            jsonPath("$.date") { value("2026-04-30") }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `PUT schedules - endTime 포함 시간 배치 성공`() {
        val response = ScheduleResponse(id = 1L, todoId = 1L, date = today, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30))
        given(scheduleService.assign(eq(10L), any())).willReturn(response)

        mockMvc.put("/api/schedules") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssignScheduleRequest(todoId = 1L, date = today, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30))
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.endTime") { value("10:30:00") }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `PUT schedules - todoId 누락 시 400 반환`() {
        mockMvc.put("/api/schedules") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"startTime": "07:00:00"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `PUT schedules - startTime 누락 시 400 반환`() {
        mockMvc.put("/api/schedules") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"todoId": 1}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT schedules - 인증 없으면 401 반환`() {
        mockMvc.put("/api/schedules") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                AssignScheduleRequest(todoId = 1L, startTime = LocalTime.of(7, 0))
            )
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `GET schedules - 날짜별 일정 시각화 조회 성공`() {
        val items = listOf(
            DailyScheduleItemResponse(1, 1L, "운동", false, LocalTime.of(7, 0)),
            DailyScheduleItemResponse(2, 2L, "독서", true, null),
        )
        given(scheduleService.getByDate(10L, today)).willReturn(items)

        mockMvc.get("/api/schedules") {
            param("date", "2026-04-30")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("운동") }
            jsonPath("$[1].startTime") { value(null) }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `DELETE schedules-todoId - 시간 배치 취소 성공 시 204 반환`() {
        mockMvc.delete("/api/schedules/1").andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `DELETE schedules-todoId - 인증 없으면 401 반환`() {
        mockMvc.delete("/api/schedules/1").andExpect {
            status { isUnauthorized() }
        }
    }
}
