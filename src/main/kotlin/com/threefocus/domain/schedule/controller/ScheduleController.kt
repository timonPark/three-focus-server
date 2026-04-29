package com.threefocus.domain.schedule.controller

import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.DailyScheduleItemResponse
import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.service.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Schedule", description = "시간 배치 API")
@RestController
@RequestMapping("/api/schedules")
class ScheduleController(private val scheduleService: ScheduleService) {

    @Operation(summary = "할 일 시간 배치")
    @PutMapping
    fun assign(
        @AuthenticationPrincipal user: UserDetails,
        @Valid @RequestBody request: AssignScheduleRequest,
    ): ScheduleResponse = scheduleService.assign(user.username.toLong(), request)

    @Operation(summary = "날짜별 일정 시각화 조회")
    @GetMapping
    fun getByDate(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): List<DailyScheduleItemResponse> = scheduleService.getByDate(user.username.toLong(), date)
}
