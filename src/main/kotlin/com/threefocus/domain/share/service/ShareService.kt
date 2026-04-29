package com.threefocus.domain.share.service

import com.threefocus.domain.schedule.repository.ScheduleQueryRepository
import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.dto.ShareResponse
import com.threefocus.domain.share.dto.SharedScheduleResponse
import com.threefocus.domain.share.dto.SharedTop3ItemResponse
import com.threefocus.domain.share.entity.Share
import com.threefocus.domain.share.repository.ShareQueryRepository
import com.threefocus.domain.share.repository.ShareRepository
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

        val top3Items = top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(share.userId, share.date)
        val items = top3Items.map { top3 ->
            val todo = todoQueryRepository.findById(top3.todoId)!!
            val schedule = scheduleQueryRepository.findByTodoId(top3.todoId)
            SharedTop3ItemResponse(
                orderIndex = top3.orderIndex,
                title = todo.title,
                isCompleted = todo.isCompleted,
                startTime = schedule?.startTime,
            )
        }

        return SharedScheduleResponse(
            shareToken = share.shareToken,
            date = share.date,
            top3 = items,
        )
    }
}
