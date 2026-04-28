package com.threefocus.domain.top3.repository

import com.threefocus.domain.top3.entity.Top3
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface Top3Repository : JpaRepository<Top3, Long> {

    @Modifying
    @Query("DELETE FROM Top3 t WHERE t.userId = :userId AND t.date = :date")
    fun deleteAllByUserIdAndDate(@Param("userId") userId: Long, @Param("date") date: LocalDate)
}
