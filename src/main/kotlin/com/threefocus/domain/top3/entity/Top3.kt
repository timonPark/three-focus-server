package com.threefocus.domain.top3.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "top3", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "date", "order_index"])])
class Top3(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val todoId: Long,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int,
)
