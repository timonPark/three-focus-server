package com.threefocus.domain.share.dto

import com.threefocus.domain.share.entity.Share
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateShareRequest(
    val date: LocalDate,
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
