<!-- part 2/3 of dsl-idioms.md -->

## Scope Functions

```kotlin
// let - transform and null check
val result = user?.let { u ->
    "${u.name} (${u.email})"
}

// run - execute block and return result
val greeting = run {
    val name = getName()
    val title = getTitle()
    "$title $name"
}

// with - operate on object
val message = with(user) {
    "User: $name, Email: $email, Active: $isActive"
}

// apply - configure object
val user = User().apply {
    name = "John"
    email = "john@example.com"
    isActive = true
}

// also - side effects
val saved = user
    .also { logger.info("Saving user: ${it.name}") }
    .also { validate(it) }
    .also { repository.save(it) }

// takeIf/takeUnless - conditional returns
val adult = user.takeIf { it.age >= 18 }
val minor = user.takeUnless { it.age >= 18 }
```

## Extension Functions

```kotlin
// String extensions
fun String.isValidEmail(): Boolean =
    matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$"))

fun String.truncate(length: Int, ellipsis: String = "..."): String =
    if (this.length <= length) this
    else "${take(length - ellipsis.length)}$ellipsis"

// Collection extensions
fun <T> List<T>.second(): T = this[1]

fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Double): Double {
    var sum = 0.0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

// Generic extensions
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T =
    if (condition) apply(block) else this

// Usage
val email = "user@example.com"
    .applyIf(email.isValidEmail()) {
        toLowerCase()
    }
```

## Delegated Properties

```kotlin
import kotlin.properties.Delegates

// Lazy initialization
class Repository {
    val database: Database by lazy {
        Database.connect("jdbc:postgresql://localhost/db")
    }
}

// Observable property
class User {
    var name: String by Delegates.observable("<not set>") { prop, old, new ->
        println("${prop.name} changed from $old to $new")
    }
}

// Vetoable property (can reject changes)
class Account {
    var balance: Double by Delegates.vetoable(0.0) { _, old, new ->
        new >= 0 // Only allow non-negative balance
    }
}

// Custom delegate
class Preference<T>(private val key: String, private val default: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        preferences.get(key) as? T ?: default

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        preferences.set(key, value)
    }
}

class Settings {
    var theme: String by Preference("theme", "light")
    var fontSize: Int by Preference("fontSize", 14)
}

// Map delegation
class UserData(map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
    val email: String by map
}

val userData = UserData(
    mapOf(
        "name" to "John",
        "age" to 30,
        "email" to "john@example.com"
    )
)
```

## Infix Functions

```kotlin
// Custom infix operators
infix fun <T> T.shouldBe(expected: T) {
    if (this != expected) {
        throw AssertionError("Expected $expected but got $this")
    }
}

infix fun String.matches(regex: Regex): Boolean =
    this.matches(regex)

// Usage
val result = 2 + 2
result shouldBe 4

"test@example.com" matches Regex(".*@.*\\..*")

// DSL with infix
class Route(val path: String) {
    infix fun to(handler: () -> Unit): RouteDefinition =
        RouteDefinition(path, handler)
}

data class RouteDefinition(val path: String, val handler: () -> Unit)

infix fun String.GET(handler: () -> Unit): RouteDefinition =
    Route(this) to handler

// Usage
val route = "/users" GET { println("Get users") }
```

## Operator Overloading

