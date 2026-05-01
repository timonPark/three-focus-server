package com.threefocus.domain.term.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "terms")
class Term(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val type: TermType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val isRequired: Boolean,

    @Column(nullable = false)
    val version: String,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
