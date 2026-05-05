<!-- part 2/2 of multiplatform-kmp.md -->


actual class DatabaseDriver() {
    private val db = // Initialize iOS SQLite
}

actual suspend fun DatabaseDriver.query(sql: String): List<Map<String, Any>> =
    withContext(Dispatchers.Default) {
        // iOS-specific query execution
    }
```

## Ktor Client Multiplatform

```kotlin
// commonMain
class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun getUsers(): List<User> =
        client.get("https://api.example.com/users").body()

    suspend fun createUser(user: User): User =
        client.post("https://api.example.com/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
}
```

## Source Set Hierarchy

```kotlin
// Intermediate source sets for iOS
kotlin {
    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val iosMain by creating {
            dependsOn(commonMain)
        }

        val iosX64Main by getting {
            dependsOn(iosMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}
```

## Native Interop (iOS)

```kotlin
// iosMain - Calling Objective-C/Swift
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication

fun getAppVersion(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "Unknown"

fun openURL(url: String) {
    val nsUrl = NSURL.URLWithString(url)
    UIApplication.sharedApplication.openURL(nsUrl ?: return)
}

// Freezing for thread safety (Kotlin/Native memory model)
class IosViewModel {
    private val scope = MainScope()

    fun loadData() {
        scope.launch {
            val data = api.getData().freeze() // Freeze for iOS
            updateUI(data)
        }
    }
}
```

## Testing Multiplatform Code

```kotlin
// commonTest
class UserRepositoryTest {
    private lateinit var repository: UserRepository

    @BeforeTest
    fun setup() {
        repository = UserRepository(FakeApiService())
    }

    @Test
    fun testLoadUsers() = runTest {
        repository.loadUsers()

        val users = repository.users.value
        assertEquals(2, users.size)
    }
}

// Platform-specific tests
// androidTest
class AndroidUserRepositoryTest {
    @Test
    fun testAndroidSpecific() {
        // Android-only test
    }
}

// iosTest
class IosUserRepositoryTest {
    @Test
    fun testIosSpecific() {
        // iOS-only test
    }
}
```

## Publishing KMP Library

```kotlin
// build.gradle.kts
plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("kotlinMultiplatform") {
            groupId = "com.example"
            artifactId = "shared"
            version = "1.0.0"
        }
    }

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/user/repo")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

## Quick Reference

| Pattern | Purpose |
|---------|---------|
| `expect class` | Declare platform-specific type in common |
| `actual class` | Implement platform-specific type |
| `commonMain` | Shared code across all platforms |
| `androidMain` | Android-specific implementations |
| `iosMain` | iOS-specific implementations (all targets) |
| `jvmMain` | JVM/Desktop-specific code |
| `jsMain` | JavaScript-specific code |
| `*Test` | Platform-specific tests |
| `dependsOn` | Source set hierarchy |
| `.freeze()` | iOS memory model (legacy) |
| `kotlin("multiplatform")` | KMP Gradle plugin |
