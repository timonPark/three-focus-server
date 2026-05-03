package com.threefocus.domain.todo.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "todos")
class Todo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(nullable = true)
    var memo: String? = null,

    @Column(nullable = true)
    var estimatedMinutes: Int? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
