## Data Layer Refactoring - Architecture Overview

### What Changed

This refactoring restructures the data layer to follow Android best practices using:
1. **Proper Error Handling** with comprehensive error types
2. **Network Abstraction** via `RemoteDataSource`
3. **Coordinated Repositories** that manage data sources
4. **Retry Logic** with exponential backoff
5. **Separation of Concerns** between API calls, network handling, and business logic

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│          Domain Layer (Repositories)                │
│  AuthRepository, UserRepository, CoursesRepository  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│      Data Layer - Repository Pattern                │
│  Coordinates Local & Remote Data Sources            │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼────────────┐  ┌─────▼──────────┐
│  RemoteDataSource  │  │  (Future)      │
│  Implements all    │  │  LocalDataSource│
│  DataSource ifaces │  │  (Room DB)     │
└────────┬───────────┘  └────────────────┘
         │
┌────────▼──────────────────────────────────────────┐
│    Network Layer - Error Handling & Retry         │
│  NetworkCallHandler + RetryPolicy                 │
└────────┬──────────────────────────────────────────┘
         │
┌────────▼──────────────────────────────────────────┐
│      Retrofit API Interface                        │
│  RetrofitApiService (Low-level HTTP calls)        │
└──────────────────────────────────────────────────┘
```

---

## Key Components

### 1. Enhanced Error Types (`AppError`)
- `NetworkError` - Network connectivity issues
- `Unauthorized` - 401 Unauthorized
- `Forbidden` - 403 Forbidden (new)
- `NotFound` - 404 Not Found
- `BadRequest` - 400 Bad Request (new)
- `ServerError` - 5xx Server errors
- `ServiceUnavailable` - 503 Service Unavailable (new)
- `Timeout` - Request timeout (new)
- `DecodingError` - JSON parsing failure
- `ValidationError` - Field validation errors (new)
- `Unknown` - Unclassified errors

**New Properties:**
- `isRetryable: Boolean` - Whether the error warrants a retry
- `isCritical: Boolean` - Whether it's an auth/permission issue
- `errorDescription: String` - Arabic user-friendly messages

### 2. Retry Policy (`RetryPolicy.kt`)
```kotlin
// Built-in policies
RetryPolicy.DEFAULT          // 3 retries, 100ms initial delay
RetryPolicy.AGGRESSIVE       // 5 retries, 50ms initial delay  
RetryPolicy.CONSERVATIVE     // 1 retry, 200ms initial delay

// Exponential backoff: delay = min(delay * 2.0, maxDelayMs)
```

**Usage:**
```kotlin
val result = retryPolicy.execute {
    networkCall()
}
```

### 3. Network Call Handler (`NetworkCallHandler.kt`)
Centralized error mapping with two methods:

```kotlin
// With automatic retry
NetworkCallHandler.safeCall(retryPolicy) { 
    retrofit.someCall() 
}

// Without retry
NetworkCallHandler.executeCall { 
    retrofit.someCall() 
}
```

**Features:**
- Maps HTTP status codes to `AppError`
- Catches exceptions (timeout, IO, etc.)
- Supports retry with exponential backoff
- Handles null response bodies

### 4. Data Source Interfaces
Segregated by domain feature:
- `AuthDataSource` - Login
- `UserDataSource` - Profile, performance overview
- `CoursesDataSource` - Course operations
- `AssignmentsDataSource` - Assignment CRUD
- `NotificationsDataSource` - Notification ops
- `CertificatesDataSource` - Certificate ops
- `LeaderboardDataSource` - Leaderboard
- `BadgesDataSource` - User badges
- `EventsDataSource` - Upcoming events
- `StatsDataSource` - Training statistics
- `SearchDataSource` - Course search
- `ActivityDataSource` - Activity tracking

### 5. Remote Data Source (`RemoteDataSource.kt`)
Single implementation class that implements all data source interfaces.

**Responsibilities:**
- Wraps Retrofit calls with `NetworkCallHandler`
- Applies retry policies
- Gets current user ID from DataStore
- No business logic - pure API calls

**Example:**
```kotlin
class RemoteDataSource(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager,
    private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT
) : AuthDataSource, UserDataSource, CoursesDataSource, ... {
    
    override suspend fun login(username: String, password: String) {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.login(username, password)
        }
    }
}
```

### 6. Refactored Repositories
**Before:** Thin pass-through to API service
**After:** Coordinate data sources, add caching logic

**Example - AuthRepository:**
```kotlin
class AuthRepository(
    private val remoteDataSource: AuthDataSource,
    private val dataStoreManager: DataStoreManager,
    private val localMock: MockApiService? = null
) : AuthRepositoryProtocol {
    
    override suspend fun login(username, password) {
        val result = remoteDataSource.login(username, password)
        return when (result) {
            is Success -> {
                dataStoreManager.save(KEY_TOKEN, result.data.token)
                result
            }
            is Failure -> {
                if (shouldFallback(result.error)) {
                    localMock?.login(username, password)
                } else result
            }
        }
    }
}
```

**Responsibilities:**
- Cache management
- Fallback strategies
- Data synchronization
- Cross-source business logic

### 7. Dependency Injection (`AppDependencies`)
Updated service locator to:
1. Create `RemoteDataSource` once with retry policies
2. Inject same data source into multiple repositories
3. Each repository gets only the interfaces it needs

```kotlin
val remoteDataSource = RemoteDataSource(retrofit, dataStore, retryPolicy)

val userRepository = UserRepository(
    userDataSource = remoteDataSource,
    badgesDataSource = remoteDataSource,
    leaderboardDataSource = remoteDataSource,
    eventsDataSource = remoteDataSource,
    statsDataSource = remoteDataSource
)
```

---

## Benefits

### ✅ Better Testability
- Data sources are interfaces → easy mocking
- Repositories depend on interfaces, not implementations
- No hidden dependencies

### ✅ Clearer Error Handling
- Type-safe `AppError` sealed class
- `isRetryable` and `isCritical` properties
- Centralized error mapping

### ✅ Automatic Retry
- Exponential backoff built-in
- Configurable retry policies per data source
- Smart retry logic (doesn't retry 4xx errors)

### ✅ Better Separation of Concerns
- Network layer handles HTTP + retry + error mapping
- Repository layer handles caching + fallbacks + business logic
- Data sources are pure API adapters

### ✅ Future Scalability
- Ready for local caching (add LocalDataSource)
- Ready for pagination (add pagination params to data sources)
- Ready for Hilt migration (interfaces all in place)

---

## Migration Path

### Already Done
✅ Steps 2 & 3 & 5 complete:
- Created data source interfaces
- Implemented RemoteDataSource
- Refactored repositories to use data sources
- Enhanced error handling with retry logic

### Future Steps
⏳ Step 1: Local Database (Room)
- Create entity models mirroring domain models
- Build LocalDataSource implementing same interfaces
- Add cache invalidation strategies

⏳ Step 4: Hilt Dependency Injection
- Replace AppDependencies with Hilt modules
- Enable constructor injection
- Remove service locator pattern

---

## Usage Example

### In ViewModel/Presenter:
```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    suspend fun login(username: String, password: String) {
        val result = authRepository.login(username, password)
        when (result) {
            is AppResult.Success -> {
                // Show success
            }
            is AppResult.Failure -> {
                val message = result.error.errorDescription
                // Show error with Arabic message
            }
        }
    }
}
```

### Direct Usage:
```kotlin
val deps = AppDependencies.getInstance(context)
val coursesResult = deps.coursesRepository.getEnrolledCourses()
```

---

## Deprecated Components

The following are now deprecated but kept for backward compatibility:
- `RealApiService` → Use `RemoteDataSource` instead
- `AppDependencies.apiService` → Use specific repositories instead

---

## Next Steps

1. **Add Unit Tests** for data sources and repositories
2. **Implement LocalDataSource** with Room database
3. **Migrate to Hilt** for better dependency injection
4. **Add Pagination Support** to handle large datasets
5. **Cache Invalidation** strategies based on time or events

---

Generated: June 11, 2026
Architecture: CLEAN with Repository pattern + Data Source pattern

