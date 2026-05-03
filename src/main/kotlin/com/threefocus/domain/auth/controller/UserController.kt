package com.threefocus.domain.auth.controller

import com.threefocus.domain.auth.dto.UserResponse
import com.threefocus.domain.auth.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal user: UserDetails): UserResponse =
        userService.getMe(user.username.toLong())
}
