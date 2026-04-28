package com.threefocus.domain.todo.repository

import com.threefocus.domain.todo.entity.Todo
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class TodoQueryRepository(private val dsl: DSLContext) {

    fun findById(id: Long): Todo? =
        dsl.select()
            .from(DSL.table("todos"))
            .where(DSL.field("id", Long::class.java).eq(id))
            .fetchOne()
            ?.map { mapToTodo(it) }

    fun findAllByUserIdAndDate(userId: Long, date: LocalDate): List<Todo> =
        dsl.select()
            .from(DSL.table("todos"))
            .where(
                DSL.field("user_id", Long::class.java).eq(userId)
                    .and(DSL.field("date", LocalDate::class.java).eq(date))
            )
            .fetch { mapToTodo(it) }

    private fun mapToTodo(record: Record) = Todo(
        id = record.get("id", Long::class.java),
        userId = record.get("user_id", Long::class.java),
        title = record.get("title", String::class.java),
        isCompleted = record.get("is_completed", Boolean::class.java),
        date = record.get("date", LocalDate::class.java),
        createdAt = record.get("created_at", LocalDateTime::class.java),
    )
}
