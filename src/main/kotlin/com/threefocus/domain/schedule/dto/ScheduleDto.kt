package com.threefocus.domain.schedule.dto

import com.threefocus.domain.schedule.entity.Schedule
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class AssignScheduleRequest(
    @field:NotNull val todoId: Long?,
    val date: LocalDate? = null,
    @field:NotNull val startTime: LocalTime?,
    val endTime: LocalTime? = null,
)

data class ScheduleResponse(
    val id: Long,
    val todoId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime?,
) {
    companion object {
        fun from(schedule: Schedule) = ScheduleResponse(
            id = schedule.id,
            todoId = schedule.todoId,
            date = schedule.date,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
        )
    }
}

data class DailyScheduleItemResponse(
    val orderIndex: Int,
    val todoId: Long,
    val title: String,
    val isCompleted: Boolean,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
)
