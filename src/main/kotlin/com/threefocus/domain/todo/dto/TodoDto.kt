package com.threefocus.domain.todo.dto

import com.threefocus.domain.todo.entity.Todo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateTodoRequest(
    @field:NotBlank val title: String,
    @field:NotNull val date: LocalDate?,
)

data class UpdateTodoRequest(
    val title: String?,
    val isCompleted: Boolean?,
)

data class TodoResponse(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val date: LocalDate,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(todo: Todo) = TodoResponse(
            id = todo.id,
            title = todo.title,
            isCompleted = todo.isCompleted,
            date = todo.date,
            createdAt = todo.createdAt,
        )
    }
}
