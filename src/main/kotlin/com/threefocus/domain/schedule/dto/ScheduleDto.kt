package com.threefocus.domain.schedule.dto

import com.threefocus.domain.schedule.entity.Schedule
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class AssignScheduleRequest(
    @field:NotNull val todoId: Long?,
    @field:NotNull val startTime: LocalTime?,
)

data class ScheduleResponse(
    val id: Long,
    val todoId: Long,
    val startTime: LocalTime,
) {
    companion object {
        fun from(schedule: Schedule) = ScheduleResponse(
            id = schedule.id,
            todoId = schedule.todoId,
            startTime = schedule.startTime,
        )
    }
}

data class DailyScheduleItemResponse(
    val orderIndex: Int,
    val todoId: Long,
    val title: String,
    val isCompleted: Boolean,
    val startTime: LocalTime?,
)
