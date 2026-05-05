<!-- part 3/3 of ktor-server.md -->

## Error Handling

```kotlin
import io.ktor.server.plugins.statuspages.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Nothing>(success = false, error = cause.message)
                    )
                }
                is NotFoundException -> {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Nothing>(success = false, error = cause.message)
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Nothing>(success = false, error = "Internal server error")
                    )
                }
            }
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse<Nothing>(success = false, error = "Resource not found")
            )
        }
    }
}

class NotFoundException(message: String) : Exception(message)
```

## CORS Configuration

```kotlin
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowNonSimpleContentTypes = true

        anyHost() // Development only
        // allowHost("client-host", schemes = listOf("http", "https"))
    }
}
```

## WebSockets

```kotlin
import io.ktor.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/chat") {
            val session = ChatSession(this)
            chatService.addSession(session)

            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val message = frame.readText()
                            chatService.broadcast(message)
                        }
                        else -> {}
                    }
                }
            } finally {
                chatService.removeSession(session)
            }
        }
    }
}
```

## Testing

```kotlin
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testGetUsers() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }

        val response = client.get("/api/v1/users")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testCreateUser() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }

        val response = client.post("/api/v1/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("test@example.com", "Test User", "password123"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testAuthenticatedRoute() = testApplication {
        application {
            configureAuth()
            configureRouting()
        }

        val token = generateToken("user123")

        val response = client.get("/api/v1/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}
```

## Quick Reference

| Plugin | Purpose |
|--------|---------|
| `ContentNegotiation` | JSON serialization |
| `Authentication` | JWT/OAuth2 auth |
| `CORS` | Cross-origin requests |
| `StatusPages` | Error handling |
| `CallLogging` | Request logging |
| `WebSockets` | WebSocket support |
| `RateLimit` | Rate limiting |
| `Compression` | Response compression |

| Function | Purpose |
|----------|---------|
| `call.receive<T>()` | Parse request body |
| `call.respond()` | Send response |
| `call.parameters` | Query/path params |
| `call.principal()` | Get authenticated user |
| `authenticate { }` | Protect routes |
| `route("/path") { }` | Group routes |
