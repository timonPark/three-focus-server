package com.threefocus.domain.top3.dto

import com.threefocus.domain.top3.entity.Top3
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class SetTop3Request(
    @field:Size(min = 1, max = 3) val todoIds: List<Long>,
    val date: LocalDate,
)

data class Top3Response(
    val id: Long,
    val todoId: Long,
    val date: LocalDate,
    val orderIndex: Int,
) {
    companion object {
        fun from(top3: Top3) = Top3Response(
            id = top3.id,
            todoId = top3.todoId,
            date = top3.date,
            orderIndex = top3.orderIndex,
        )
    }
}
