package com.threefocus.domain.schedule.repository

import com.threefocus.domain.schedule.entity.Schedule
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduleRepository : JpaRepository<Schedule, Long> {

    fun deleteByTodoId(todoId: Long)
}
