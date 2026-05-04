package com.threefocus.domain.share.dto

import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.share.entity.Share
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.top3.entity.Top3
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateShareRequest(
    @field:NotNull val date: LocalDate?,
)

data class ShareResponse(
    val id: Long,
    val shareToken: String,
    val date: LocalDate,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(share: Share) = ShareResponse(
            id = share.id,
            shareToken = share.shareToken,
            date = share.date,
            createdAt = share.createdAt,
        )
    }
}

data class SharedScheduleResponse(
    val shareToken: String,
    val date: LocalDate,
    val todos: List<TodoResponse>,
    val schedules: List<ScheduleResponse>,
    val top3Data: List<SharedTop3Response>,
)

data class SharedTop3Response(
    val id: Long,
    val todoId: Long,
    val date: LocalDate,
    val order: Int,
) {
    companion object {
        fun from(top3: Top3) = SharedTop3Response(
            id = top3.id,
            todoId = top3.todoId,
            date = top3.date,
            order = top3.orderIndex,
        )
    }
}
