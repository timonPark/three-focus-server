package com.threefocus.domain.todo.repository

import com.threefocus.domain.todo.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<Todo, Long>
