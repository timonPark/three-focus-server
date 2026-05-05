<!-- part 2/2 of coroutines-flow.md -->

## Cancellation

```kotlin
suspend fun cancellableWork() {
    withTimeout(5000) {
        while (isActive) { // Check for cancellation
            doWork()
            yield() // Cooperation point
        }
    }
}

// Cleanup with finally
suspend fun withCleanup() {
    try {
        longRunningTask()
    } finally {
        withContext(NonCancellable) {
            cleanup() // Always runs even if cancelled
        }
    }
}
```

## Testing Coroutines

```kotlin
import kotlinx.coroutines.test.*

class UserViewModelTest {
    @Test
    fun testLoadUsers() = runTest {
        val viewModel = UserViewModel(fakeRepository)

        viewModel.loadUsers()
        advanceUntilIdle() // Run all pending coroutines

        assertEquals(expectedUsers, viewModel.users.value)
    }

    @Test
    fun testFlow() = runTest {
        val flow = repository.getUsersFlow()
        val results = flow.take(3).toList()

        assertEquals(3, results.size)
    }

    // Testing with Turbine
    @Test
    fun testFlowWithTurbine() = runTest {
        repository.getUsersFlow().test {
            assertEquals(Loading, awaitItem())
            assertEquals(Success(users), awaitItem())
            awaitComplete()
        }
    }
}
```

## Performance Patterns

```kotlin
// Use sequence for lazy evaluation
fun processLargeList(items: List<Item>): List<Result> =
    items.asSequence()
        .filter { it.isValid }
        .map { transform(it) }
        .take(100)
        .toList() // Only processes first 100 valid items

// Channel for producer-consumer
fun produceNumbers() = produce {
    repeat(10) {
        send(it)
        delay(100)
    }
}

// Parallel processing with async
suspend fun processInParallel(items: List<Item>): List<Result> =
    coroutineScope {
        items.map { item ->
            async { process(item) }
        }.awaitAll()
    }
```

## Quick Reference

| Pattern | Use Case |
|---------|----------|
| `launch` | Fire-and-forget coroutine |
| `async/await` | Parallel computation with result |
| `flow { }` | Cold stream of values |
| `StateFlow` | Hot flow with current state |
| `SharedFlow` | Hot flow for events |
| `withContext` | Switch dispatcher |
| `supervisorScope` | Independent child failures |
| `coroutineScope` | All children must succeed |
| `flowOn` | Change flow dispatcher |
| `catch` | Handle flow errors |
| `retry` | Retry on failure |
| `debounce` | Rate limiting |
| `distinctUntilChanged` | Skip duplicates |
| `combine` | Merge multiple flows |
