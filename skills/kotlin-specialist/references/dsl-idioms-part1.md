<!-- part 1/3 of dsl-idioms.md -->

# DSL & Kotlin Idioms

## Type-Safe Builders

```kotlin
// HTML DSL example
class Tag(val name: String) {
    val children = mutableListOf<Tag>()
    val attributes = mutableMapOf<String, String>()

    fun <T : Tag> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun toString(): String {
        val attrs = attributes.entries.joinToString(" ") { "${it.key}=\"${it.value}\"" }
        val content = children.joinToString("")
        return "<$name${if (attrs.isNotEmpty()) " $attrs" else ""}>$content</$name>"
    }
}

class HTML : Tag("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head : Tag("head") {
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
}

class Title : Tag("title") {
    operator fun String.unaryPlus() {
        children.add(TextNode(this))
    }
}

class Body : Tag("body") {
    fun div(classes: String? = null, init: Div.() -> Unit) =
        initTag(Div(), init).apply {
            classes?.let { attributes["class"] = it }
        }
}

class Div : Tag("div") {
    fun p(init: P.() -> Unit) = initTag(P(), init)
}

class P : Tag("p") {
    operator fun String.unaryPlus() {
        children.add(TextNode(this))
    }
}

class TextNode(private val text: String) : Tag("") {
    override fun toString() = text
}

// Usage
fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}

val page = html {
    head {
        title { +"My Page" }
    }
    body {
        div("container") {
            p { +"Hello, World!" }
        }
    }
}
```

## Lambda with Receiver

```kotlin
// Configuration DSL
class DatabaseConfig {
    var host: String = "localhost"
    var port: Int = 5432
    var username: String = ""
    var password: String = ""
    var database: String = ""
}

fun database(config: DatabaseConfig.() -> Unit): DatabaseConfig {
    return DatabaseConfig().apply(config)
}

// Usage
val dbConfig = database {
    host = "db.example.com"
    port = 3306
    username = "admin"
    password = "secret"
    database = "myapp"
}

// Builder pattern with type-safe DSL
class User private constructor(
    val id: String,
    val name: String,
    val email: String,
    val age: Int?
) {
    class Builder {
        var id: String = ""
        var name: String = ""
        var email: String = ""
        var age: Int? = null

        fun build(): User {
            require(id.isNotBlank()) { "ID is required" }
            require(name.isNotBlank()) { "Name is required" }
            require(email.isNotBlank()) { "Email is required" }
            return User(id, name, email, age)
        }
    }
}

fun user(init: User.Builder.() -> Unit): User =
    User.Builder().apply(init).build()

// Usage
val user = user {
    id = "123"
    name = "John Doe"
    email = "john@example.com"
    age = 30
}
```

