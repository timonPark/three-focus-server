package com.threefocus.domain.top3.dto

import com.threefocus.domain.top3.entity.Top3
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class SetTop3Request(
    @field:Size(min = 1, max = 3) val todoIds: List<Long>,
    @field:NotNull val date: LocalDate?,
)

data class Top3Response(
    val id: Long,
    val todoId: Long,
    val date: LocalDate,
    val order: Int,
) {
    companion object {
        fun from(top3: Top3) = Top3Response(
            id = top3.id,
            todoId = top3.todoId,
            date = top3.date,
            order = top3.orderIndex,
        )
    }
}
