package com.threefocus.domain.auth.repository

import com.threefocus.domain.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
