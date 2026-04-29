package com.threefocus.domain.top3.service

import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.top3.dto.SetTop3Request
import com.threefocus.domain.top3.dto.Top3Response
import com.threefocus.domain.top3.entity.Top3
import com.threefocus.domain.top3.repository.Top3QueryRepository
import com.threefocus.domain.top3.repository.Top3Repository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class Top3Service(
    private val top3Repository: Top3Repository,
    private val top3QueryRepository: Top3QueryRepository,
    private val todoQueryRepository: TodoQueryRepository,
) {
    @Transactional
    fun setTop3(userId: Long, request: SetTop3Request): List<Top3Response> {
        if (request.todoIds.size > 3) throw ApiException(ErrorCode.TOP3_LIMIT_EXCEEDED)
        request.todoIds.forEach { todoId ->
            val todo = todoQueryRepository.findById(todoId) ?: throw ApiException(ErrorCode.TODO_NOT_FOUND)
            if (todo.userId != userId) throw ApiException(ErrorCode.FORBIDDEN)
        }
        top3Repository.deleteAllByUserIdAndDate(userId, request.date!!)
        val saved = request.todoIds.mapIndexed { index, todoId ->
            top3Repository.save(Top3(userId = userId, todoId = todoId, date = request.date!!, orderIndex = index + 1))
        }
        return saved.map { Top3Response.from(it) }
    }

    @Transactional(readOnly = true)
    fun getTop3(userId: Long, date: LocalDate): List<Top3Response> =
        top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(userId, date).map { Top3Response.from(it) }
}
