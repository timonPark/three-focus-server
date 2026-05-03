package com.threefocus.domain.todo.dto

import com.threefocus.domain.todo.entity.Todo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateTodoRequest(
    @field:NotBlank val title: String,
    @field:NotNull val date: LocalDate?,
    val memo: String? = null,
    val estimatedMinutes: Int? = null,
)

data class UpdateTodoRequest(
    val title: String? = null,
    val completed: Boolean? = null,
    val memo: String? = null,
    val estimatedMinutes: Int? = null,
)

data class TodoResponse(
    val id: Long,
    val title: String,
    val memo: String?,
    val estimatedMinutes: Int?,
    val date: LocalDate,
    val completed: Boolean,
    val isTop3: Boolean,
    val top3Order: Int?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(todo: Todo, isTop3: Boolean = false, top3Order: Int? = null) = TodoResponse(
            id = todo.id,
            title = todo.title,
            memo = todo.memo,
            estimatedMinutes = todo.estimatedMinutes,
            date = todo.date,
            completed = todo.isCompleted,
            isTop3 = isTop3,
            top3Order = top3Order,
            createdAt = todo.createdAt,
        )
    }
}
