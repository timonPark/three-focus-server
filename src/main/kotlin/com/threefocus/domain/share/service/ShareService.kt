package com.threefocus.domain.share.service

import com.threefocus.domain.share.dto.CreateShareRequest
import com.threefocus.domain.share.dto.ShareResponse
import com.threefocus.domain.share.entity.Share
import com.threefocus.domain.share.repository.ShareQueryRepository
import com.threefocus.domain.share.repository.ShareRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShareService(
    private val shareRepository: ShareRepository,
    private val shareQueryRepository: ShareQueryRepository,
) {
    @Transactional
    fun createShare(userId: Long, request: CreateShareRequest): ShareResponse {
        val share = shareRepository.save(Share(userId = userId, date = request.date))
        return ShareResponse.from(share)
    }

    @Transactional(readOnly = true)
    fun getShare(shareToken: String): ShareResponse {
        val share = shareQueryRepository.findByShareToken(shareToken)
            ?: throw NoSuchElementException("공유 일정을 찾을 수 없습니다.")
        return ShareResponse.from(share)
    }
}
