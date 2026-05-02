package com.threefocus.global.security

import com.threefocus.global.exception.ApiException
import com.threefocus.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val name: String,
    val emailVerified: Boolean,
)

@Component
class GoogleTokenVerifier(
    @Value("\${google.client-id:}") private val clientId: String,
) {
    private val restTemplate = RestTemplate()

    fun verify(idToken: String): GoogleUserInfo {
        val response = runCatching {
            restTemplate.getForObject(
                "https://oauth2.googleapis.com/tokeninfo?id_token=$idToken",
                Map::class.java,
            )
        }.getOrElse { throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN) }
            ?: throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN)

        if (clientId.isNotBlank() && response["aud"] != clientId) {
            throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN)
        }

        val emailVerified = response["email_verified"]?.toString()?.lowercase() == "true"
        if (!emailVerified) {
            throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN)
        }

        return GoogleUserInfo(
            sub = response["sub"]?.toString() ?: throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN),
            email = response["email"]?.toString() ?: throw ApiException(ErrorCode.INVALID_GOOGLE_TOKEN),
            name = response["name"]?.toString() ?: response["email"]?.toString()!!,
            emailVerified = emailVerified,
        )
    }
}
