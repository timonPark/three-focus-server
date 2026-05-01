package com.threefocus.domain.auth.repository

import com.threefocus.domain.auth.entity.Gender
import com.threefocus.domain.auth.entity.User
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class UserQueryRepository(private val dsl: DSLContext) {

    fun findById(id: Long): User? =
        dsl.select()
            .from(DSL.table("users"))
            .where(DSL.field("id", Long::class.java).eq(id))
            .fetchOne()
            ?.map { mapToUser(it) }

    fun findByEmail(email: String): User? =
        dsl.select()
            .from(DSL.table("users"))
            .where(DSL.field("email", String::class.java).eq(email))
            .fetchOne()
            ?.map { mapToUser(it) }

    fun existsByEmail(email: String): Boolean =
        dsl.fetchExists(
            DSL.selectOne()
                .from(DSL.table("users"))
                .where(DSL.field("email", String::class.java).eq(email))
        )

    private fun mapToUser(record: org.jooq.Record) = User(
        id = record.get("id", Long::class.java),
        email = record.get("email", String::class.java),
        password = record.get("password", String::class.java),
        name = record.get("name", String::class.java),
        phone = record.get("phone", String::class.java),
        gender = Gender.valueOf(record.get("gender", String::class.java)),
        birthday = record.get("birthday", LocalDate::class.java),
        createdAt = record.get("created_at", LocalDateTime::class.java),
    )
}
