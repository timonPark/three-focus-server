package com.threefocus.domain.term.repository

import com.threefocus.domain.term.entity.Term
import org.springframework.data.jpa.repository.JpaRepository

interface TermRepository : JpaRepository<Term, Long> {
    fun findAllByIsRequired(isRequired: Boolean): List<Term>
}
