package com.threefocus.domain.share.service

import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.dto.ShareResponse
import com.threefocus.domain.share.dto.SharedScheduleResponse
import com.threefocus.domain.share.dto.SharedTop3Response
import com.threefocus.domain.share.entity.Share
import com.threefocus.domain.share.repository.ShareQueryRepository
import com.threefocus.domain.share.repository.ShareRepository
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.top3.repository.Top3QueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShareService(
    private val shareRepository: ShareRepository,
    private val shareQueryRepository: ShareQueryRepository,
    private val top3QueryRepository: Top3QueryRepository,
    private val todoQueryRepository: TodoQueryRepository,
    private val scheduleQueryRepository: ScheduleQueryRepository,
) {
    @Transactional
    fun createShare(userId: Long, request: CreateShareRequest): ShareResponse {
        val share = shareRepository.save(Share(userId = userId, date = request.date!!))
        return ShareResponse.from(share)
    }

    @Transactional(readOnly = true)
    fun getShare(shareToken: String): SharedScheduleResponse {
        val share = shareQueryRepository.findByShareToken(shareToken)
            ?: throw ApiException(ErrorCode.NOT_FOUND)

        val top3List = top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(share.userId, share.date)
        val top3ByTodoId = top3List.associateBy { it.todoId }

        val todos = todoQueryRepository.findAllByUserIdAndDate(share.userId, share.date)
        val scheduleByTodoId = scheduleQueryRepository
            .findAllByTodoIds(todos.map { it.id })
            .associateBy { it.todoId }

        val todoResponses = todos.map { todo ->
            val top3 = top3ByTodoId[todo.id]
            TodoResponse.from(todo, isTop3 = top3 != null, top3Order = top3?.orderIndex)
        }

        val scheduleResponses = scheduleByTodoId.values.map { ScheduleResponse.from(it) }

        val top3Responses = top3List.map { SharedTop3Response.from(it) }

        return SharedScheduleResponse(
            shareToken = share.shareToken,
            date = share.date,
            todos = todoResponses,
            schedules = scheduleResponses,
            top3Data = top3Responses,
        )
    }
}
