package com.threefocus.domain.schedule.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "schedules")
class Schedule(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val todoId: Long,

    @Column(nullable = false)
    var startTime: LocalTime,
)
