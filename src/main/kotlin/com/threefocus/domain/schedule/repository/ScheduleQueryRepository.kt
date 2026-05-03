package com.threefocus.domain.schedule.repository

import com.threefocus.domain.schedule.dto.DailyScheduleItemResponse
import com.threefocus.domain.schedule.entity.Schedule
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime

@Repository
class ScheduleQueryRepository(private val dsl: DSLContext) {

    fun findByTodoId(todoId: Long): Schedule? =
        dsl.select()
            .from(DSL.table("schedules"))
            .where(DSL.field("todo_id", Long::class.java).eq(todoId))
            .fetchOne()
            ?.map { mapToSchedule(it) }

    fun findDailyScheduleByUserIdAndDate(userId: Long, date: LocalDate): List<DailyScheduleItemResponse> =
        dsl.select(
            DSL.field("t3.order_index"),
            DSL.field("t3.todo_id"),
            DSL.field("td.title"),
            DSL.field("td.is_completed"),
            DSL.field("s.start_time"),
        )
            .from(DSL.table("top3").`as`("t3"))
            .join(DSL.table("todos").`as`("td")).on(DSL.field("t3.todo_id").eq(DSL.field("td.id")))
            .leftJoin(DSL.table("schedules").`as`("s")).on(DSL.field("s.todo_id").eq(DSL.field("td.id")))
            .where(
                DSL.field("t3.user_id", Long::class.java).eq(userId)
                    .and(DSL.field("t3.date", LocalDate::class.java).eq(date))
            )
            .orderBy(DSL.field("t3.order_index").asc())
            .fetch { record ->
                DailyScheduleItemResponse(
                    orderIndex = record.get("order_index", Int::class.java),
                    todoId = record.get("todo_id", Long::class.java),
                    title = record.get("title", String::class.java),
                    isCompleted = record.get("is_completed", Boolean::class.java),
                    startTime = record.get("start_time", LocalTime::class.java),
                )
            }

    private fun mapToSchedule(record: Record) = Schedule(
        id = record.get("id", Long::class.java),
        todoId = record.get("todo_id", Long::class.java),
        date = record.get("date", LocalDate::class.java),
        startTime = record.get("start_time", LocalTime::class.java),
        endTime = record.get("end_time", LocalTime::class.java),
    )
}
