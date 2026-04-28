package com.threefocus.domain.schedule.controller

import com.threefocus.domain.schedule.dto.AssignScheduleRequest
import com.threefocus.domain.schedule.dto.ScheduleResponse
import com.threefocus.domain.schedule.service.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Schedule", description = "시간 배치 API")
@RestController
@RequestMapping("/api/schedules")
class ScheduleController(private val scheduleService: ScheduleService) {

    @Operation(summary = "할 일 시간 배치")
    @PutMapping
    fun assign(@RequestBody request: AssignScheduleRequest): ScheduleResponse =
        scheduleService.assign(request)
}
