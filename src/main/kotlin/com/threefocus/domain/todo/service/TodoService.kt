package com.threefocus.domain.todo.service

import com.threefocus.domain.schedule.repository.ScheduleRepository
import com.threefocus.domain.todo.dto.CreateTodoRequest
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.todo.dto.UpdateTodoRequest
import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.todo.repository.TodoRepository
import com.threefocus.domain.top3.repository.Top3QueryRepository
import com.threefocus.domain.top3.repository.Top3Repository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val todoQueryRepository: TodoQueryRepository,
    private val scheduleRepository: ScheduleRepository,
    private val top3Repository: Top3Repository,
    private val top3QueryRepository: Top3QueryRepository,
) {
    @Transactional
    fun create(userId: Long, request: CreateTodoRequest): TodoResponse {
        val todo = todoRepository.save(
            Todo(
                userId = userId,
                title = request.title,
                date = request.date!!,
                memo = request.memo,
                estimatedMinutes = request.estimatedMinutes,
            )
        )
        return TodoResponse.from(todo)
    }

    @Transactional(readOnly = true)
    fun getByDate(userId: Long, date: LocalDate): List<TodoResponse> {
        val todos = todoQueryRepository.findAllByUserIdAndDate(userId, date)
        val top3Map = top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(userId, date)
            .associate { it.todoId to it.orderIndex }
        return todos.map { todo ->
            TodoResponse.from(todo, isTop3 = top3Map.containsKey(todo.id), top3Order = top3Map[todo.id])
        }
    }

    @Transactional
    fun update(userId: Long, todoId: Long, request: UpdateTodoRequest): TodoResponse {
        val todo = findOwned(userId, todoId)
        val updated = todoRepository.save(
            Todo(
                id = todo.id,
                userId = todo.userId,
                title = request.title ?: todo.title,
                isCompleted = request.completed ?: todo.isCompleted,
                date = todo.date,
                memo = request.memo ?: todo.memo,
                estimatedMinutes = request.estimatedMinutes ?: todo.estimatedMinutes,
                createdAt = todo.createdAt,
            )
        )
        val top3 = top3QueryRepository.findAllByUserIdAndDateOrderByOrderIndex(userId, todo.date)
            .find { it.todoId == todoId }
        return TodoResponse.from(updated, isTop3 = top3 != null, top3Order = top3?.orderIndex)
    }

    @Transactional
    fun delete(userId: Long, todoId: Long) {
        val todo = findOwned(userId, todoId)
        scheduleRepository.deleteByTodoId(todo.id)
        top3Repository.deleteByTodoId(todo.id)
        todoRepository.deleteById(todo.id)
    }

    private fun findOwned(userId: Long, todoId: Long): Todo {
        val todo = todoQueryRepository.findById(todoId) ?: throw ApiException(ErrorCode.TODO_NOT_FOUND)
        if (todo.userId != userId) throw ApiException(ErrorCode.FORBIDDEN)
        return todo
    }
}
