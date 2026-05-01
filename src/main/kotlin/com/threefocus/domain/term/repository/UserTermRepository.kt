package com.threefocus.domain.term.repository

import com.threefocus.domain.term.entity.UserTerm
import org.springframework.data.jpa.repository.JpaRepository

interface UserTermRepository : JpaRepository<UserTerm, Long>
