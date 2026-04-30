package com.threefocus.domain.todo.service

import com.threefocus.domain.schedule.repository.ScheduleRepository
import com.threefocus.domain.todo.dto.CreateTodoRequest
import com.threefocus.domain.todo.dto.UpdateTodoRequest
import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.todo.repository.TodoRepository
import com.threefocus.domain.top3.repository.Top3Repository
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

@ExtendWith(MockitoExtension::class)
class TodoServiceTest {

    @Mock private lateinit var todoRepository: TodoRepository
    @Mock private lateinit var todoQueryRepository: TodoQueryRepository
    @Mock private lateinit var scheduleRepository: ScheduleRepository
    @Mock private lateinit var top3Repository: Top3Repository

    @InjectMocks private lateinit var todoService: TodoService

    private val today = LocalDate.of(2026, 4, 30)
    private val todo = Todo(id = 1L, userId = 10L, title = "운동하기", date = today)

    @Test
    fun `create - 성공 시 TodoResponse 반환`() {
        given(todoRepository.save(any())).willReturn(todo)

        val result = todoService.create(10L, CreateTodoRequest("운동하기", today))

        assertThat(result.title).isEqualTo("운동하기")
        assertThat(result.isCompleted).isFalse()
    }

    @Test
    fun `getByDate - 해당 날짜의 할 일 목록 반환`() {
        given(todoQueryRepository.findAllByUserIdAndDate(10L, today)).willReturn(listOf(todo))

        val result = todoService.getByDate(10L, today)

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("운동하기")
    }

    @Test
    fun `update - 성공 시 수정된 TodoResponse 반환`() {
        val updated = Todo(id = 1L, userId = 10L, title = "독서하기", isCompleted = true, date = today)
        given(todoQueryRepository.findById(1L)).willReturn(todo)
        given(todoRepository.save(any())).willReturn(updated)

        val result = todoService.update(10L, 1L, UpdateTodoRequest(title = "독서하기", isCompleted = true))

        assertThat(result.title).isEqualTo("독서하기")
        assertThat(result.isCompleted).isTrue()
    }

    @Test
    fun `update - 존재하지 않는 할 일 수정 시 TODO_NOT_FOUND 예외`() {
        given(todoQueryRepository.findById(99L)).willReturn(null)

        val ex = assertThrows<ApiException> {
            todoService.update(10L, 99L, UpdateTodoRequest(title = "수정"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TODO_NOT_FOUND)
    }

    @Test
    fun `update - 타인의 할 일 수정 시 FORBIDDEN 예외`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        val ex = assertThrows<ApiException> {
            todoService.update(99L, 1L, UpdateTodoRequest(title = "수정"))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }

    @Test
    fun `delete - 성공 시 연관 Schedule과 Top3도 함께 삭제`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        todoService.delete(10L, 1L)

        then(scheduleRepository).should().deleteByTodoId(1L)
        then(top3Repository).should().deleteByTodoId(1L)
        then(todoRepository).should().deleteById(1L)
    }

    @Test
    fun `delete - 타인의 할 일 삭제 시 FORBIDDEN 예외`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo)

        val ex = assertThrows<ApiException> {
            todoService.delete(99L, 1L)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }
}
