<!-- part 3/3 of android-compose.md -->

## Remember & State

```kotlin
@Composable
fun SearchScreen() {
    // State hoisting
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Result>>(emptyList()) }

    Column {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                // Trigger search
            }
        )

        ResultsList(results)
    }
}

// Remember with keys
@Composable
fun UserDetail(userId: String) {
    val user = remember(userId) {
        loadUser(userId)
    }

    // rememberSaveable survives process death
    var expanded by rememberSaveable { mutableStateOf(false) }
}
```

## Animation

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.*

@Composable
fun AnimatedContent() {
    var visible by remember { mutableStateOf(false) }

    // Simple fade
    AnimatedVisibility(visible) {
        Text("Hello World")
    }

    // Custom animation
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    // Animated content
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            fadeIn() + slideInVertically() togetherWith
                    fadeOut() + slideOutVertically()
        }
    ) { tab ->
        when (tab) {
            0 -> HomeContent()
            1 -> ProfileContent()
        }
    }
}
```

## Performance Optimization

```kotlin
// Stability annotations
@Immutable
data class User(val id: String, val name: String)

@Stable
class UserState(private val repository: UserRepository) {
    val users: StateFlow<List<User>> = repository.users
}

// Key for recomposition optimization
@Composable
fun ItemList(items: List<Item>) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            ItemCard(item)
        }
    }
}

// derivedStateOf for expensive calculations
@Composable
fun FilteredList(items: List<Item>, filter: String) {
    val filtered by remember(items, filter) {
        derivedStateOf {
            items.filter { it.name.contains(filter, ignoreCase = true) }
        }
    }

    LazyColumn {
        items(filtered) { item ->
            ItemCard(item)
        }
    }
}
```

## Quick Reference

| Composable | Purpose |
|------------|---------|
| `remember` | Retain value across recompositions |
| `rememberSaveable` | Survive process death |
| `LaunchedEffect` | Run suspend functions |
| `DisposableEffect` | Cleanup when leaving |
| `SideEffect` | Non-suspend effects |
| `derivedStateOf` | Computed state |
| `collectAsStateWithLifecycle` | Flow to State (lifecycle-aware) |
| `animateFloatAsState` | Animate value changes |
| `LazyColumn` | Scrollable list |
| `Scaffold` | Material 3 layout structure |
| `viewModelScope` | ViewModel coroutine scope |
| `@HiltViewModel` | Hilt dependency injection |
