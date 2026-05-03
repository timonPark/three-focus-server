package com.threefocus.domain.auth.service

import com.threefocus.domain.auth.dto.UserResponse
import com.threefocus.domain.auth.repository.UserQueryRepository
import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userQueryRepository: UserQueryRepository,
) {
    @Transactional(readOnly = true)
    fun getMe(userId: Long): UserResponse {
        val user = userQueryRepository.findById(userId) ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        return UserResponse.from(user)
    }
}
