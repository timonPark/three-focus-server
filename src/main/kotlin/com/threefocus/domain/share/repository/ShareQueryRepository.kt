package com.threefocus.domain.share.repository

import com.threefocus.domain.share.entity.Share
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class ShareQueryRepository(private val dsl: DSLContext) {

    fun findByShareToken(shareToken: String): Share? =
        dsl.select()
            .from(DSL.table("shares"))
            .where(DSL.field("share_token", String::class.java).eq(shareToken))
            .fetchOne()
            ?.map { mapToShare(it) }

    private fun mapToShare(record: Record) = Share(
        id = record.get("id", Long::class.java),
        userId = record.get("user_id", Long::class.java),
        date = record.get("date", LocalDate::class.java),
        shareToken = record.get("share_token", String::class.java),
        createdAt = record.get("created_at", LocalDateTime::class.java),
    )
}
