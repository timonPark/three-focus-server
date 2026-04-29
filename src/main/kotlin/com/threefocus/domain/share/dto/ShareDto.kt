package com.threefocus.domain.share.dto

import com.threefocus.domain.share.entity.Share
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateShareRequest(
    @field:NotNull val date: LocalDate?,
)

data class ShareResponse(
    val id: Long,
    val shareToken: String,
    val date: LocalDate,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(share: Share) = ShareResponse(
            id = share.id,
            shareToken = share.shareToken,
            date = share.date,
            createdAt = share.createdAt,
        )
    }
}

data class SharedScheduleResponse(
    val shareToken: String,
    val date: LocalDate,
    val top3: List<SharedTop3ItemResponse>,
)

data class SharedTop3ItemResponse(
    val orderIndex: Int,
    val title: String,
    val isCompleted: Boolean,
    val startTime: LocalTime?,
)
