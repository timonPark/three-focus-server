package com.threefocus.domain.schedule.service

import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.DailyScheduleItemResponse
import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.entity.Schedule
import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.schedule.repository.ScheduleRepository
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleQueryRepository: ScheduleQueryRepository,
    private val todoQueryRepository: TodoQueryRepository,
) {
    @Transactional
    fun assign(userId: Long, request: AssignScheduleRequest): ScheduleResponse {
        val todo = todoQueryRepository.findById(request.todoId!!)
            ?: throw ApiException(ErrorCode.TODO_NOT_FOUND)
        if (todo.userId != userId) throw ApiException(ErrorCode.FORBIDDEN)

        val existing = scheduleQueryRepository.findByTodoId(request.todoId)
        val schedule = scheduleRepository.save(
            Schedule(
                id = existing?.id ?: 0,
                todoId = request.todoId,
                startTime = request.startTime!!,
            )
        )
        return ScheduleResponse.from(schedule)
    }

    @Transactional(readOnly = true)
    fun getByDate(userId: Long, date: LocalDate): List<DailyScheduleItemResponse> =
        scheduleQueryRepository.findDailyScheduleByUserIdAndDate(userId, date)

    @Transactional
    fun remove(userId: Long, todoId: Long) {
        val todo = todoQueryRepository.findById(todoId)
            ?: throw ApiException(ErrorCode.TODO_NOT_FOUND)
        if (todo.userId != userId) throw ApiException(ErrorCode.FORBIDDEN)
        scheduleRepository.deleteByTodoId(todoId)
    }
}
