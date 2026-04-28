package com.threefocus.domain.schedule.dto

import com.threefocus.domain.schedule.entity.Schedule
import java.time.LocalTime

data class AssignScheduleRequest(
    val todoId: Long,
    val startTime: LocalTime,
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
