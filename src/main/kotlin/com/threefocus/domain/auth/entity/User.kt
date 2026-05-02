package com.threefocus.domain.auth.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    var phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var gender: Gender? = null,

    @Column(nullable = true)
    var birthday: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider = AuthProvider.LOCAL,

    @Column(nullable = true)
    val providerId: String? = null,

    @Column(nullable = false)
    var isProfileComplete: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun completeProfile(phone: String, gender: Gender, birthday: LocalDate) {
        this.phone = phone
        this.gender = gender
        this.birthday = birthday
        this.isProfileComplete = true
    }
}

enum class Gender {
    MALE, FEMALE
}

enum class AuthProvider {
    LOCAL, GOOGLE
}
