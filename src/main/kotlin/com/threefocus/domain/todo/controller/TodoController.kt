package com.threefocus.domain.todo.controller

import com.threefocus.domain.todo.dto.CreateTodoRequest
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.todo.dto.UpdateTodoRequest
import com.threefocus.domain.todo.service.TodoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Todo", description = "할 일 API")
@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {

    @Operation(summary = "할 일 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody request: CreateTodoRequest,
    ): TodoResponse = todoService.create(user.username.toLong(), request)

    @Operation(summary = "날짜별 할 일 조회")
    @GetMapping
    fun getByDate(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): List<TodoResponse> = todoService.getByDate(user.username.toLong(), date)

    @Operation(summary = "할 일 수정")
    @PutMapping("/{todoId}")
    fun update(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable todoId: Long,
        @RequestBody request: UpdateTodoRequest,
    ): TodoResponse = todoService.update(user.username.toLong(), todoId, request)

    @Operation(summary = "할 일 삭제")
    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable todoId: Long,
    ) = todoService.delete(user.username.toLong(), todoId)
}
