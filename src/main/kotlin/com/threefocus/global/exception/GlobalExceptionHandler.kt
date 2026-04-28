package com.threefocus.global.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ErrorResponse> {
        val code = e.errorCode
        return ResponseEntity.status(code.status).body(ErrorResponse(code.name, code.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "입력값이 올바르지 않습니다."
        return ResponseEntity.badRequest().body(ErrorResponse(ErrorCode.INVALID_INPUT.name, message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val code = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(code.status).body(ErrorResponse(code.name, code.message))
    }
}

data class ErrorResponse(val code: String, val message: String)
