package com.threefocus.domain.share.controller

import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.dto.ShareResponse
import com.threefocus.domain.share.service.ShareService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "Share", description = "일정 공유 API")
@RestController
@RequestMapping("/api/shares")
class ShareController(private val shareService: ShareService) {

    @Operation(summary = "공유 링크 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createShare(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody request: CreateShareRequest,
    ): ShareResponse = shareService.createShare(user.username.toLong(), request)

    @Operation(summary = "공유 일정 조회")
    @GetMapping("/{shareToken}")
    fun getShare(@PathVariable shareToken: String): ShareResponse =
        shareService.getShare(shareToken)
}
