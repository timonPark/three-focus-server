package com.threefocus.domain.top3.controller

import com.threefocus.domain.top3.dto.SetTop3Request
import com.threefocus.domain.top3.dto.Top3Response
import com.threefocus.domain.top3.service.Top3Service
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Top3", description = "오늘의 Top3 API")
@RestController
@RequestMapping("/api/top3")
class Top3Controller(private val top3Service: Top3Service) {

    @Operation(summary = "Top3 지정")
    @PostMapping
    fun setTop3(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody request: SetTop3Request,
    ): List<Top3Response> = top3Service.setTop3(user.username.toLong(), request)

    @Operation(summary = "날짜별 Top3 조회")
    @GetMapping
    fun getTop3(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): List<Top3Response> = top3Service.getTop3(user.username.toLong(), date)
}
