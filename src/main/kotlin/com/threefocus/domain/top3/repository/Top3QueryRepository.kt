package com.threefocus.domain.top3.repository

import com.threefocus.domain.top3.entity.Top3
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class Top3QueryRepository(private val dsl: DSLContext) {

    fun findAllByUserIdAndDateOrderByOrderIndex(userId: Long, date: LocalDate): List<Top3> =
        dsl.select()
            .from(DSL.table("top3"))
            .where(
                DSL.field("user_id", Long::class.java).eq(userId)
                    .and(DSL.field("date", LocalDate::class.java).eq(date))
            )
            .orderBy(DSL.field("order_index").asc())
            .fetch { mapToTop3(it) }

    private fun mapToTop3(record: Record) = Top3(
        id = record.get("id", Long::class.java),
        userId = record.get("user_id", Long::class.java),
        todoId = record.get("todo_id", Long::class.java),
        date = record.get("date", LocalDate::class.java),
        orderIndex = record.get("order_index", Int::class.java),
    )
}
