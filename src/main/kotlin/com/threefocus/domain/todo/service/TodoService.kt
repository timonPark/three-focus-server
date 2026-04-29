package com.threefocus.domain.todo.service

import com.threefocus.domain.schedule.repository.ScheduleRepository
import com.threefocus.domain.todo.dto.CreateTodoRequest
import com.threefocus.domain.todo.dto.TodoResponse
import com.threefocus.domain.todo.dto.UpdateTodoRequest
import com.threefocus.domain.todo.entity.Todo
import com.threefocus.domain.todo.repository.TodoQueryRepository
import com.threefocus.domain.todo.repository.TodoRepository
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
) {
    @Transactional
    fun create(userId: Long, request: CreateTodoRequest): TodoResponse {
        val todo = todoRepository.save(Todo(userId = userId, title = request.title, date = request.date!!))
        return TodoResponse.from(todo)
    }

    @Transactional(readOnly = true)
    fun getByDate(userId: Long, date: LocalDate): List<TodoResponse> =
        todoQueryRepository.findAllByUserIdAndDate(userId, date).map { TodoResponse.from(it) }

    @Transactional
    fun update(userId: Long, todoId: Long, request: UpdateTodoRequest): TodoResponse {
        val todo = findOwned(userId, todoId)
        val updated = todoRepository.save(
            Todo(
                id = todo.id,
                userId = todo.userId,
                title = request.title ?: todo.title,
                isCompleted = request.isCompleted ?: todo.isCompleted,
                date = todo.date,
                createdAt = todo.createdAt,
            )
        )
        return TodoResponse.from(updated)
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
