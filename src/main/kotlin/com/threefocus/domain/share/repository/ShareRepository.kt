package com.threefocus.domain.share.repository

import com.threefocus.domain.share.entity.Share
import org.springframework.data.jpa.repository.JpaRepository

interface ShareRepository : JpaRepository<Share, Long>
