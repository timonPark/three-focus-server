<!-- part 1/2 of coroutines-flow.md -->

# Coroutines & Flow API

## Structured Concurrency

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserRepository(
    private val api: ApiService,
    private val scope: CoroutineScope
) {
    // CORRECT: Structured concurrency with supervisor
    suspend fun fetchUsers(): Result<List<User>> = coroutineScope {
        supervisorScope {
            try {
                val users = async { api.getUsers() }
                val profiles = async { api.getProfiles() }
                Result.success(users.await() + profiles.await())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // WRONG: GlobalScope bypasses structured concurrency
    // fun fetchUsersWrong() = GlobalScope.launch { ... }
}
```

## Coroutine Scopes & Dispatchers

```kotlin
class ViewModel : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    fun loadData() {
        launch {
            val data = withContext(Dispatchers.IO) {
                // I/O operations on IO dispatcher
                repository.fetchData()
            }
            // Back to Main dispatcher automatically
            updateUI(data)
        }
    }

    fun cleanup() {
        coroutineContext.cancelChildren()
    }
}

// Android ViewModel - use viewModelScope
class AndroidViewModel : ViewModel() {
    fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers().collect { users ->
                _uiState.update { it.copy(users = users) }
            }
        }
    }
}
```

## Flow Basics

```kotlin
// Cold flow - starts on collection
fun getUsers(): Flow<List<User>> = flow {
    val users = api.fetchUsers()
    emit(users)
    delay(1000)
    emit(users + api.fetchNewUsers())
}.flowOn(Dispatchers.IO)

// Hot flow - StateFlow (always has value)
class UserStore {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    suspend fun loadUsers() {
        api.getUsers().collect { userList ->
            _users.update { userList }
        }
    }
}

// Hot flow - SharedFlow (events, no initial value)
class EventBus {
    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()

    suspend fun emit(event: Event) {
        _events.emit(event)
    }
}
```

## Flow Operators

```kotlin
fun getUsersWithPosts(): Flow<UserWithPosts> = flow {
    userRepository.getUsers()
        .map { user -> UserWithPosts(user, getPosts(user.id)) }
        .filter { it.posts.isNotEmpty() }
        .catch { e -> emit(UserWithPosts.Error(e)) }
        .onEach { delay(100) } // Throttle
        .distinctUntilChanged()
        .collect { emit(it) }
}

// Combining flows
fun getCombinedData(): Flow<UiState> = combine(
    userFlow,
    settingsFlow,
    notificationsFlow
) { user, settings, notifications ->
    UiState(user, settings, notifications)
}

// Flattening flows
fun searchUsers(query: String): Flow<List<User>> =
    queryFlow
        .debounce(300)
        .filter { it.length >= 3 }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.search(query)
        }
```

## Exception Handling

```kotlin
suspend fun loadDataSafely(): Result<Data> =
    supervisorScope {
        try {
            val result = async {
                api.getData()
            }
            Result.success(result.await())
        } catch (e: CancellationException) {
            // Don't catch cancellation - rethrow
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

// Flow error handling
fun getDataFlow(): Flow<Data> = flow {
    emit(api.getData())
}.retry(3) { cause ->
    cause is IOException
}.catch { e ->
    emit(Data.Error(e))
}

// Supervisor scope for independent children
suspend fun loadMultiple() = supervisorScope {
    val job1 = launch { task1() } // Failure won't affect job2
    val job2 = launch { task2() }
    joinAll(job1, job2)
}
```

