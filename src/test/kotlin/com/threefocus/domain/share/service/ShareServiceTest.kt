package com.threefocus.domain.share.service

import com.threefocus.domain.schedule.entity.Schedule
import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.entity.Share
import com.threefocus.domain.share.repository.ShareQueryRepository
import com.threefocus.domain.share.repository.ShareRepository
import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.top3.entity.Top3
import com.threefocus.domain.top3.repository.Top3QueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ShareServiceTest {

    @Mock private lateinit var shareRepository: ShareRepository
    @Mock private lateinit var shareQueryRepository: ShareQueryRepository
    @Mock private lateinit var top3QueryRepository: Top3QueryRepository
    @Mock private lateinit var todoQueryRepository: TodoQueryRepository
    @Mock private lateinit var scheduleQueryRepository: ScheduleQueryRepository

    @InjectMocks private lateinit var shareService: ShareService

    private val today = LocalDate.of(2026, 4, 30)
    private val userId = 10L
    private val token = "test-token-uuid"
    private val share = Share(id = 1L, userId = userId, date = today, shareToken = token)

    @Test
    fun `createShare - 성공 시 ShareResponse 반환`() {
        given(shareRepository.save(any())).willReturn(share)

        val result = shareService.createShare(userId, CreateShareRequest(today))

        assertThat(result.shareToken).isEqualTo(token)
        assertThat(result.date).isEqualTo(today)
    }

    @Test
    fun `getShare - 성공 시 todos, top3Data, schedules 포함한 응답 반환`() {
        val top3List = listOf(
            Top3(id = 1L, userId = userId, todoId = 1L, date = today, orderIndex = 1),
            Top3(id = 2L, userId = userId, todoId = 2L, date = today, orderIndex = 2),
        )
        val todo1 = Todo(id = 1L, userId = userId, title = "운동", isCompleted = true, date = today)
        val todo2 = Todo(id = 2L, userId = userId, title = "독서", isCompleted = false, date = today)
        val todos = listOf(todo1, todo2)

        val schedules = listOf(
            Schedule(id = 10L, todoId = 1L, date = today, startTime = LocalTime.of(7, 0), endTime = LocalTime.of(8, 0)),
        )

        given(shareQueryRepository.findByShareToken(token)).willReturn(share)
        given(top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(userId, today)).willReturn(top3List)
        given(todoQueryRepository.findAllByUserIdAndDate(userId, today)).willReturn(todos)
        given(scheduleQueryRepository.findAllByTodoIds(listOf(1L, 2L))).willReturn(schedules)

        val result = shareService.getShare(token)

        assertThat(result.shareToken).isEqualTo(token)

        assertThat(result.todos).hasSize(2)
        assertThat(result.todos[0].title).isEqualTo("운동")
        assertThat(result.todos[0].completed).isTrue()
        assertThat(result.todos[0].isTop3).isTrue()
        assertThat(result.todos[0].top3Order).isEqualTo(1)
        assertThat(result.todos[1].isTop3).isTrue()
        assertThat(result.todos[1].top3Order).isEqualTo(2)

        assertThat(result.top3Data).hasSize(2)
        assertThat(result.top3Data[0].order).isEqualTo(1)
        assertThat(result.top3Data[0].todoId).isEqualTo(1L)
        assertThat(result.top3Data[1].order).isEqualTo(2)

        assertThat(result.schedules).hasSize(1)
        assertThat(result.schedules[0].todoId).isEqualTo(1L)
        assertThat(result.schedules[0].startTime).isEqualTo(LocalTime.of(7, 0))
        assertThat(result.schedules[0].endTime).isEqualTo(LocalTime.of(8, 0))
    }

    @Test
    fun `getShare - 존재하지 않는 토큰으로 조회 시 NOT_FOUND 예외`() {
        given(shareQueryRepository.findByShareToken("invalid-token")).willReturn(null)

        val ex = assertThrows<ApiException> {
            shareService.getShare("invalid-token")
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.NOT_FOUND)
    }
}
