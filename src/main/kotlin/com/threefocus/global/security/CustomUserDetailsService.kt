package com.threefocus.global.security

import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userQueryRepository: UserQueryRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userQueryRepository.findById(username.toLong())
            ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        return User.withUsername(user.id.toString())
            .password(user.password ?: "")
            .roles("USER")
            .build()
    }
}
