package com.threefocus.domain.todo.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.threefocus.domain.todo.dto.CreateTodoRequest
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.todo.dto.UpdateTodoRequest
import com.threefocus.domain.todo.service.TodoService
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(TodoController::class)
@Import(SecurityConfig::class)
class TodoControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    @MockBean private lateinit var todoService: TodoService
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var userDetailsService: UserDetailsService

    private val today = LocalDate.of(2026, 4, 30)
    private val todoResponse = TodoResponse(
        id = 1L, title = "운동하기", isCompleted = false, date = today, createdAt = LocalDateTime.now()
    )

    @Test
    @WithMockUser(username = "10")
    fun `POST todos - 성공 시 201 반환`() {
        given(todoService.create(eq(10L), any())).willReturn(todoResponse)

        mockMvc.post("/api/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateTodoRequest("운동하기", today))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("운동하기") }
            jsonPath("$.isCompleted") { value(false) }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `POST todos - 제목 공백 시 400 반환`() {
        mockMvc.post("/api/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateTodoRequest("", today))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `POST todos - 날짜 누락 시 400 반환`() {
        mockMvc.post("/api/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "운동하기"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST todos - 인증 없으면 401 반환`() {
        mockMvc.post("/api/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateTodoRequest("운동하기", today))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `GET todos - 날짜별 목록 조회 성공`() {
        given(todoService.getByDate(10L, today)).willReturn(listOf(todoResponse))

        mockMvc.get("/api/todos") {
            param("date", "2026-04-30")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("운동하기") }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `PUT todos-id - 수정 성공 시 200 반환`() {
        val updated = todoResponse.copy(title = "독서하기", isCompleted = true)
        given(todoService.update(eq(10L), eq(1L), any())).willReturn(updated)

        mockMvc.put("/api/todos/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateTodoRequest(title = "독서하기", isCompleted = true))
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("독서하기") }
            jsonPath("$.isCompleted") { value(true) }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `PUT todos-id - 타인 할 일 수정 시 403 반환`() {
        given(todoService.update(eq(10L), eq(1L), any()))
            .willThrow(ApiException(ErrorCode.FORBIDDEN))

        mockMvc.put("/api/todos/1") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateTodoRequest(title = "수정"))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(username = "10")
    fun `DELETE todos-id - 삭제 성공 시 204 반환`() {
        mockMvc.delete("/api/todos/1").andExpect {
            status { isNoContent() }
        }
    }
}
