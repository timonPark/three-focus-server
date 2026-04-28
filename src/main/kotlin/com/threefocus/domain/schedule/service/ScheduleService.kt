package com.threefocus.domain.schedule.service

import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.entity.Schedule
import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.schedule.repository.ScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleQueryRepository: ScheduleQueryRepository,
) {
    @Transactional
    fun assign(request: AssignScheduleRequest): ScheduleResponse {
        val existing = scheduleQueryRepository.findByTodoId(request.todoId)
        val schedule = scheduleRepository.save(
            Schedule(
                id = existing?.id ?: 0,
                todoId = request.todoId,
                startTime = request.startTime,
            )
        )
        return ScheduleResponse.from(schedule)
    }
}
