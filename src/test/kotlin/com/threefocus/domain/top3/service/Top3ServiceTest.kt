package com.threefocus.domain.top3.service

import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.top3.dto.SetTop3Request
import com.threefocus.domain.top3.entity.Top3
import com.threefocus.domain.top3.repository.Top3QueryRepository
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
class Top3ServiceTest {

    @Mock private lateinit var top3Repository: Top3Repository
    @Mock private lateinit var top3QueryRepository: Top3QueryRepository
    @Mock private lateinit var todoQueryRepository: TodoQueryRepository

    @InjectMocks private lateinit var top3Service: Top3Service

    private val today = LocalDate.of(2026, 4, 30)
    private val userId = 10L
    private val todo1 = Todo(id = 1L, userId = userId, title = "운동", date = today)
    private val todo2 = Todo(id = 2L, userId = userId, title = "독서", date = today)

    @Test
    fun `setTop3 - 성공 시 Top3 목록 반환`() {
        given(todoQueryRepository.findById(1L)).willReturn(todo1)
        given(todoQueryRepository.findById(2L)).willReturn(todo2)
        given(top3Repository.save(any())).willAnswer { it.arguments[0] as Top3 }

        val result = top3Service.setTop3(userId, SetTop3Request(listOf(1L, 2L), today))

        assertThat(result).hasSize(2)
        then(top3Repository).should().deleteAllByUserIdAndDate(userId, today)
    }

    @Test
    fun `setTop3 - 4개 이상 지정 시 TOP3_LIMIT_EXCEEDED 예외`() {
        val ex = assertThrows<ApiException> {
            top3Service.setTop3(userId, SetTop3Request(listOf(1L, 2L, 3L, 4L), today))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TOP3_LIMIT_EXCEEDED)
    }

    @Test
    fun `setTop3 - 존재하지 않는 할 일 지정 시 TODO_NOT_FOUND 예외`() {
        given(todoQueryRepository.findById(99L)).willReturn(null)

        val ex = assertThrows<ApiException> {
            top3Service.setTop3(userId, SetTop3Request(listOf(99L), today))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.TODO_NOT_FOUND)
    }

    @Test
    fun `setTop3 - 타인의 할 일 지정 시 FORBIDDEN 예외`() {
        val otherUserTodo = Todo(id = 1L, userId = 99L, title = "운동", date = today)
        given(todoQueryRepository.findById(1L)).willReturn(otherUserTodo)

        val ex = assertThrows<ApiException> {
            top3Service.setTop3(userId, SetTop3Request(listOf(1L), today))
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }

    @Test
    fun `getTop3 - 날짜별 Top3 순서대로 반환`() {
        val top3List = listOf(
            Top3(id = 1L, userId = userId, todoId = 1L, date = today, orderIndex = 1),
            Top3(id = 2L, userId = userId, todoId = 2L, date = today, orderIndex = 2),
        )
        given(top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(userId, today)).willReturn(top3List)

        val result = top3Service.getTop3(userId, today)

        assertThat(result).hasSize(2)
        assertThat(result[0].orderIndex).isEqualTo(1)
        assertThat(result[1].orderIndex).isEqualTo(2)
    }
}
