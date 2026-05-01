package com.threefocus.domain.term.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_terms")
class UserTerm(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val termsId: Long,

    @Column(nullable = false)
    val agreed: Boolean,

    @Column(nullable = false, updatable = false)
    val agreedAt: LocalDateTime = LocalDateTime.now(),
)
