<!-- part 2/3 of android-compose.md -->


## Navigation

```kotlin
import androidx.navigation.compose.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
```

## LazyColumn (Lists)

```kotlin
@Composable
fun UserList(
    users: List<User>,
    onUserClick: (User) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserCard(
                user = user,
                onClick = { onUserClick(user) }
            )
        }
    }
}

// Pagination with LazyColumn
@Composable
fun PaginatedList(viewModel: ListViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LazyColumn {
        items(items, key = { it.id }) { item ->
            ItemCard(item)
        }

        if (isLoading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // Load more trigger
        item {
            LaunchedEffect(Unit) {
                viewModel.loadMore()
            }
        }
    }
}
```

## Side Effects

```kotlin
@Composable
fun UserScreen(userId: String) {
    // Run once when userId changes
    LaunchedEffect(userId) {
        loadUser(userId)
    }

    // Run on every recomposition
    SideEffect {
        analyticsService.trackScreen("UserScreen")
    }

    // Cleanup when leaving composition
    DisposableEffect(Unit) {
        val listener = setupListener()
        onDispose {
            listener.cleanup()
        }
    }

    // Remember value across recompositions
    val scrollState = rememberScrollState()

    // Derived state
    val isScrolled by remember {
        derivedStateOf { scrollState.value > 0 }
    }
}
```

## Dependency Injection (Hilt)

```kotlin
// Application class
@HiltAndroidApp
class MyApplication : Application()

// Module
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiServiceImpl()

    @Provides
    @Singleton
    fun provideUserRepository(api: ApiService): UserRepository =
        UserRepositoryImpl(api)
}

// ViewModel with injection
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: String = savedStateHandle["userId"] ?: ""

    val user: StateFlow<User?> = repository
        .getUserFlow(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

// Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }
}
```

