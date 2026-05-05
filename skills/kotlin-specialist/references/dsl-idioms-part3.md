<!-- part 3/3 of dsl-idioms.md -->

```kotlin
data class Vector(val x: Double, val y: Double) {
    operator fun plus(other: Vector) =
        Vector(x + other.x, y + other.y)

    operator fun minus(other: Vector) =
        Vector(x - other.x, y - other.y)

    operator fun times(scalar: Double) =
        Vector(x * scalar, y * scalar)

    operator fun unaryMinus() =
        Vector(-x, -y)

    operator fun get(index: Int): Double = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException()
    }
}

// Usage
val v1 = Vector(1.0, 2.0)
val v2 = Vector(3.0, 4.0)
val v3 = v1 + v2
val v4 = v1 * 2.0
val x = v1[0]

// Invoke operator
class Greeter(private val greeting: String) {
    operator fun invoke(name: String) = "$greeting, $name!"
}

val greet = Greeter("Hello")
println(greet("World")) // Hello, World!
```

## Sealed Classes & When

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Exhaustive when
fun <T> handleResult(result: Result<T>): String = when (result) {
    is Result.Success -> "Data: ${result.data}"
    is Result.Error -> "Error: ${result.exception.message}"
    Result.Loading -> "Loading..."
}

// Sealed interface for more flexibility
sealed interface UiState {
    object Loading : UiState
    data class Success(val data: List<String>) : UiState
    data class Error(val message: String) : UiState
}
```

## Inline & Reified

```kotlin
// Inline function
inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val duration = System.currentTimeMillis() - start
    return result to duration
}

// Reified type parameters
inline fun <reified T> parseJson(json: String): T =
    Json.decodeFromString<T>(json)

inline fun <reified T : Any> Intent.getParcelableExtraCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as? T
    }

// Value class (inline class)
@JvmInline
value class UserId(val value: String)

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "Invalid email" }
    }
}

// Usage - zero runtime overhead
val userId = UserId("123")
val email = Email("test@example.com")
```

## Quick Reference

| Idiom | Purpose |
|-------|---------|
| `let` | Transform & null check |
| `run` | Execute block, return result |
| `with` | Operate on object |
| `apply` | Configure object |
| `also` | Side effects |
| `takeIf/takeUnless` | Conditional return |
| `by lazy` | Lazy initialization |
| `by Delegates.observable` | Observe changes |
| `inline fun` | Eliminate lambda overhead |
| `reified` | Access type at runtime |
| `@JvmInline` | Zero-cost wrapper |
| `infix` | Custom operators |
| `operator` | Operator overloading |
| `sealed class` | Restricted hierarchies |
