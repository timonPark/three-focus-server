package com.threefocus.domain.schedule.service

import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.DailyScheduleItemResponse
import com.threefocus.domain.schedule.entity.Schedule
import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.schedule.repository.ScheduleRepository
import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ScheduleServiceTest {

    @Mock private lateinit var scheduleRepository: ScheduleRepository
    @Mock private lateinit var scheduleQueryRepository: ScheduleQueryRepository
    @Mock private lateinit var todoQueryRepository: TodoQueryRepository

    @InjectMocks private lateinit var scheduleService: ScheduleService

    private val userId = 10L
    private val today = LocalDate.of(2026, 4, 30)
    private val todo = Todo(id = 1L, userId = userId, title = "운동", date = today)
    private val schedule = Schedule(id = 5L, todoId = 1L, date = today, startTime = LocalTime.of(7, 0))

    @Test
    fun `assign - 신규 시간 배치 성공`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)
        given(scheduleQueryRepository.findByTodoId(1L)).willReturn(null)
        given(scheduleRepository.save(any())).willReturn(schedule)

        val result = scheduleService.assign(userId, AssignScheduleRequest(todoId = 1L, startTime = LocalTime.of(7, 0)))

        assertThat(result.todoId).isEqualTo(1L)
        assertThat(result.startTime).isEqualTo(LocalTime.of(7, 0))
        assertThat(result.date).isEqualTo(today)
    }

    @Test
    fun `assign - 기존 시간 배치 수정(upsert) 성공`() {
        val existing = Schedule(id = 5L, todoId = 1L, date = today, startTime = LocalTime.of(6, 0))
        val updated = Schedule(id = 5L, todoId = 1L, date = today, startTime = LocalTime.of(9, 0))
        given(todoQueryRepository.findById(1L)).willReturn(todo)
        given(scheduleQueryRepository.findByTodoId(1L)).willReturn(existing)
        given(scheduleRepository.save(any())).willReturn(updated)

        val result = scheduleService.assign(userId, AssignScheduleRequest(todoId = 1L, startTime = LocalTime.of(9, 0)))

        assertThat(result.startTime).isEqualTo(LocalTime.of(9, 0))
    }

    @Test
    fun `assign - endTime 포함 배치 성공`() {
        val scheduleWithEnd = Schedule(id = 5L, todoId = 1L, date = today, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30))
        given(todoQueryRepository.findById(1L)).willReturn(todo)
        given(scheduleQueryRepository.findByTodoId(1L)).willReturn(null)
        given(scheduleRepository.save(any())).willReturn(scheduleWithEnd)

        val result = scheduleService.assign(userId, AssignScheduleRequest(todoId = 1L, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 30)))

        assertThat(result.endTime).isEqualTo(LocalTime.of(10, 30))
    }

    @Test
    fun `assign - 존재하지 않는 할 일 배치 시 TODO_NOT_FOUND 예외`() {
        given(todoQueryRepository.findById(99L)).willReturn(null)

        val ex = assertThrows<ApiException> {
            scheduleService.assign(userId, AssignScheduleRequest(todoId = 99L, startTime = LocalTime.of(7, 0)))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TODO_NOT_FOUND)
    }

    @Test
    fun `assign - 타인의 할 일 배치 시 FORBIDDEN 예외`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        val ex = assertThrows<ApiException> {
            scheduleService.assign(99L, AssignScheduleRequest(todoId = 1L, startTime = LocalTime.of(7, 0)))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }

    @Test
    fun `getByDate - 날짜별 일정 목록 반환`() {
        val items = listOf(
            DailyScheduleItemResponse(1, 1L, "운동", false, LocalTime.of(7, 0)),
            DailyScheduleItemResponse(2, 2L, "독서", true, null),
        )
        given(scheduleQueryRepository.findDailyScheduleByUserIdAndDate(userId, today)).willReturn(items)

        val result = scheduleService.getByDate(userId, today)

        assertThat(result).hasSize(2)
        assertThat(result[0].startTime).isEqualTo(LocalTime.of(7, 0))
        assertThat(result[1].startTime).isNull()
    }

    @Test
    fun `remove - 시간 배치 취소 성공`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        scheduleService.remove(userId, 1L)

        then(scheduleRepository).should().deleteByTodoId(1L)
    }

    @Test
    fun `remove - 존재하지 않는 할 일 취소 시 TODO_NOT_FOUND 예외`() {
        given(todoQueryRepository.findById(99L)).willReturn(null)

        val ex = assertThrows<ApiException> {
            scheduleService.remove(userId, 99L)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TODO_NOT_FOUND)
    }

    @Test
    fun `remove - 타인의 할 일 취소 시 FORBIDDEN 예외`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        val ex = assertThrows<ApiException> {
            scheduleService.remove(99L, 1L)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }
}
