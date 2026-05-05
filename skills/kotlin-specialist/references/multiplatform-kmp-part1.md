<!-- part 1/2 of multiplatform-kmp.md -->

# Kotlin Multiplatform (KMP)

## Project Structure

```
project/
├── commonMain/
│   ├── kotlin/
│   │   ├── data/
│   │   │   └── User.kt
│   │   ├── repository/
│   │   │   └── UserRepository.kt
│   │   └── Platform.kt (expect)
│   └── resources/
├── androidMain/
│   └── kotlin/
│       └── Platform.android.kt (actual)
├── iosMain/
│   └── kotlin/
│       └── Platform.ios.kt (actual)
└── jvmMain/
    └── kotlin/
        └── Platform.jvm.kt (actual)
```

## Gradle Configuration

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    // JVM target
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // JS target
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("io.ktor:ktor-client-core:2.3.7")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.3.7")
            }
        }
    }
}
```

## Expect/Actual Pattern

```kotlin
// commonMain/kotlin/Platform.kt
expect class Platform() {
    val name: String
    fun currentTimeMillis(): Long
}

expect fun getPlatform(): Platform

// androidMain/kotlin/Platform.android.kt
import android.os.Build

actual class Platform {
    actual val name: String = "Android ${Build.VERSION.SDK_INT}"

    actual fun currentTimeMillis(): Long =
        System.currentTimeMillis()
}

actual fun getPlatform(): Platform = Platform()

// iosMain/kotlin/Platform.ios.kt
import platform.UIKit.UIDevice
import platform.Foundation.NSDate

actual class Platform {
    actual val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    actual fun currentTimeMillis(): Long =
        (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getPlatform(): Platform = Platform()
```

## Common Code Patterns

```kotlin
// commonMain - Shared business logic
class UserRepository(private val api: ApiService) {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    suspend fun loadUsers() {
        try {
            val result = api.getUsers()
            _users.value = result
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// Shared models
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long
)

// Sealed class for platform-agnostic results
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

## Platform-Specific Implementations

```kotlin
// commonMain
expect class DatabaseDriver()

expect suspend fun DatabaseDriver.query(sql: String): List<Map<String, Any>>

// androidMain
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase

actual class DatabaseDriver(private val context: Context) {
    private val db: SupportSQLiteDatabase = // Initialize Android SQLite
}

actual suspend fun DatabaseDriver.query(sql: String): List<Map<String, Any>> =
    withContext(Dispatchers.IO) {
        // Android-specific query execution
    }

// iosMain
import platform.Foundation.NSFileManager
