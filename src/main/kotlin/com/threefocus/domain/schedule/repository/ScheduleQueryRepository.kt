package com.threefocus.domain.schedule.repository

import com.threefocus.domain.schedule.entity.Schedule
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalTime

@Repository
class ScheduleQueryRepository(private val dsl: DSLContext) {

    fun findByTodoId(todoId: Long): Schedule? =
        dsl.select()
            .from(DSL.table("schedules"))
            .where(DSL.field("todo_id", Long::class.java).eq(todoId))
            .fetchOne()
            ?.map { mapToSchedule(it) }

    private fun mapToSchedule(record: Record) = Schedule(
        id = record.get("id", Long::class.java),
        todoId = record.get("todo_id", Long::class.java),
        startTime = record.get("start_time", LocalTime::class.java),
    )
}
