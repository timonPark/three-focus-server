<!-- part 2/3 of ktor-server.md -->

## Authentication (JWT)

```kotlin
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun Application.configureAuth() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Ktor Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

// Protected routes
fun Route.protectedRoutes() {
    authenticate("auth-jwt") {
        get("/profile") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            val user = userService.getUser(userId ?: "")
            call.respond(user ?: HttpStatusCode.NotFound)
        }
    }
}

// Token generation
fun generateToken(userId: String): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000 * 60 * 24)) // 24h
        .sign(Algorithm.HMAC256(secret))
}
```

## Database Integration (Exposed)

```kotlin
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

class UserService(private val database: Database) {
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { toUser(it) }
    }

    suspend fun getUser(id: String): User? = dbQuery {
        Users.select { Users.id eq id }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    suspend fun createUser(request: CreateUserRequest): User = dbQuery {
        val id = UUID.randomUUID().toString()
        val passwordHash = hashPassword(request.password)

        Users.insert {
            it[Users.id] = id
            it[email] = request.email
            it[name] = request.name
            it[Users.passwordHash] = passwordHash
            it[createdAt] = System.currentTimeMillis()
        }

        User(id, request.email, request.name, System.currentTimeMillis())
    }

    private fun toUser(row: ResultRow): User =
        User(
            id = row[Users.id],
            email = row[Users.email],
            name = row[Users.name],
            createdAt = row[Users.createdAt]
        )
}
```

