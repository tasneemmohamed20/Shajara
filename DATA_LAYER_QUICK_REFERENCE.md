# Data Layer Quick Reference Guide

## 🚀 Common Usage Patterns

### Get Current User Profile
```kotlin
val userRepo = AppDependencies.getInstance(context).userRepository
val profileResult = userRepo.getUserProfile()

when (profileResult) {
    is AppResult.Success -> {
        val user = profileResult.data
        // Use user data
    }
    is AppResult.Failure -> {
        val errorMsg = profileResult.error.errorDescription
        showError(errorMsg)
    }
    is AppResult.Loading -> showLoadingBar()
}
```

### Login with Automatic Retry
```kotlin
val authRepo = AppDependencies.getInstance(context).authRepository
val loginResult = authRepo.login(username, password)

if (loginResult is AppResult.Success) {
    // Token already cached by repository
    val token = loginResult.data.token
    navigateToHome()
} else if (loginResult is AppResult.Failure) {
    when (loginResult.error) {
        is AppError.Unauthorized -> showInvalidCredentials()
        is AppError.NetworkError -> showNetworkError()
        is AppError.ServiceUnavailable -> showServiceUnavailable()
        else -> showGenericError(loginResult.error.errorDescription)
    }
}
```

### Fetch Courses with Error Details
```kotlin
val coursesRepo = AppDependencies.getInstance(context).coursesRepository
val coursesResult = coursesRepo.getEnrolledCourses()

val courses = coursesResult.getOrNull() ?: emptyList()
val error = coursesResult.getErrorOrNull()

if (error != null) {
    if (error.isRetryable) {
        retryFetch()
    } else if (error.isCritical) {
        handleCriticalError(error)
    }
}
```

### Search Courses (Client-Side Filtering)
```kotlin
// Internally fetches all courses, then filters client-side
val searchResult = coursesRepo.searchCourses("Python")

if (searchResult is AppResult.Success) {
    val filteredCourses = searchResult.data
    displayCourses(filteredCourses)
}
```

---

## 📦 AppResult Sealed Class Reference

```kotlin
// Success case
AppResult.Success(data = Course(...))

// Failure case
AppResult.Failure(error = AppError.NetworkError("Connection timeout"))

// Loading state
AppResult.Loading

// Utility functions
result.getOrNull()           // Returns data or null
result.getErrorOrNull()      // Returns error or null
result.isSuccess()           // true if Success
result.isFailure()           // true if Failure
result.mapSuccess { transform(data) }  // Transform success data
```

---

## 🛡️ AppError Types & Handling

### Network Errors (Retryable)
```kotlin
AppError.NetworkError("Connection refused")
AppError.Timeout
AppError.ServiceUnavailable  // 503
```
**👉 Action**: Automatically retried with exponential backoff

### HTTP Errors
```kotlin
AppError.BadRequest("Invalid input")           // 400
AppError.Unauthorized                          // 401 - Need login
AppError.Forbidden                             // 403 - No permission
AppError.NotFound                              // 404
AppError.ServerError(500, "Internal error")   // 5xx
```

### Data Errors (Non-Retryable)
```kotlin
AppError.DecodingError          // JSON parsing failed
AppError.ValidationError(fields) // Field validation failed
AppError.Unknown                 // Catch-all
```

### Error Properties
```kotlin
error.errorDescription  // Arabic user-friendly message
error.isRetryable      // Should retry (NetworkError, Timeout, 5xx)
error.isCritical       // Auth/permission issue (401, 403)
```

---

## 🔄 Retry Behavior

All errors to RemoteDataSource automatically retry based on RetryPolicy:

| Error Type | Retryable | Max Retries (Default) | Initial Delay |
|---|---|---|---|
| NetworkError | ✅ Yes | 3 | 100ms |
| Timeout | ✅ Yes | 3 | 100ms |
| ServiceUnavailable | ✅ Yes | 3 | 100ms |
| ServerError (5xx) | ✅ Yes | 3 | 100ms |
| BadRequest (400) | ❌ No | 0 | - |
| Unauthorized (401) | ❌ No | 0 | - |
| Forbidden (403) | ❌ No | 0 | - |
| NotFound (404) | ❌ No | 0 | - |
| DecodingError | ❌ No | 0 | - |

**Backoff Formula**: Next delay = min(current * 2.0, max 30s)

---

## 🏗️ Repository Architecture

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose/XML)          │
│  Activities, Fragments, ViewModels      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Repository Layer                      │
│   AuthRepository, UserRepository, ...   │
│   - Caching logic                       │
│   - Data coordination                   │
│   - Fallback strategies                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Data Source Layer (Interfaces)        │
│   AuthDataSource, UserDataSource, ...   │
│   - Service contracts                   │
│   - Easy to mock                        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   RemoteDataSource Implementation       │
│   - Wraps Retrofit calls                │
│   - Applies retry policies              │
│   - Maps errors                         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Network Layer                         │
│   - NetworkCallHandler (error mapping)  │
│   - RetryPolicy (exponential backoff)   │
│   - Retrofit                            │
└─────────────────────────────────────────┘
```

---

## 📍 Where to Find Things

### Repositories
- `AuthRepository` - Login & authentication
- `UserRepository` - User profile & stats
- `CoursesRepository` - Courses & assignments
- `NotificationsRepository` - Notifications
- `CertificatesRepository` - Certificates

### Data Sources
- `AuthDataSource` - Auth operations
- `UserDataSource` - User profile
- `CoursesDataSource` - Course data
- `AssignmentsDataSource` - Assignments
- `NotificationsDataSource` - Notifications
- `CertificatesDataSource` - Certificates
- `LeaderboardDataSource` - Leaderboard
- `BadgesDataSource` - User badges
- `EventsDataSource` - Training events
- `StatsDataSource` - Training stats
- `SearchDataSource` - Course search
- `ActivityDataSource` - Activity tracking

### Network Layer
- `AppResult` - Result wrapper (Success/Failure/Loading)
- `AppError` - Error classification
- `NetworkCallHandler` - HTTP error mapping & retry coordination
- `RetryPolicy` - Exponential backoff logic

### Storage
- `DataStoreManager` - Encrypted secure storage
- Stores tokens, user IDs, and preferences

---

## 🧪 Testing Pattern

### Mock a Data Source
```kotlin
class FakeUserDataSource : UserDataSource {
    override suspend fun getUserProfile(): AppResult<User> {
        return AppResult.Success(User(id = 1, name = "Test"))
    }
    
    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return AppResult.Success(PerformanceOverview(...))
    }
}

// Use in repository
val fakeRepo = UserRepository(
    userDataSource = FakeUserDataSource(),
    badgesDataSource = FakeBadgesDataSource(),
    // ...
)
```

### Test Error Handling
```kotlin
class ErrorUserDataSource : UserDataSource {
    override suspend fun getUserProfile(): AppResult<User> {
        return AppResult.Failure(AppError.NetworkError("No connection"))
    }
}
```

---

## ⚙️ Configuration

### Network Timeouts
Edit `NetworkConfig.kt`:
```kotlin
const val CONNECT_TIMEOUT = 30L   // seconds
const val READ_TIMEOUT = 30L
const val WRITE_TIMEOUT = 30L
```

### Retry Policies
In `AppDependencies.kt`:
```kotlin
private val defaultRetryPolicy: RetryPolicy = RetryPolicy.DEFAULT
private val aggressiveRetryPolicy: RetryPolicy = RetryPolicy.AGGRESSIVE
```

Create custom policy:
```kotlin
val customPolicy = RetryPolicy(
    maxRetries = 5,
    initialDelayMs = 200,
    maxDelayMs = 60_000,
    backoffMultiplier = 2.5
)
```

---

## 🔐 Token & Session Management

### Automatic Token Handling
```kotlin
// Token automatically cached after login
authRepository.login(username, password)  // Token cached internally

// Token automatically sent in all requests (via Interceptor)
// No need to manually add "Authorization" header

// Clear session on logout
authRepository.logout()  // Clears all cached data
```

### Check Login Status
```kotlin
val isLoggedIn = authRepository.checkUserStatus()
```

---

## 📝 Error Message Examples (Arabic)

```
NetworkError:       "الشبكة غير متصلة"
Timeout:           "انتهت المهلة الزمنية للطلب"
Unauthorized:      "انتهت الجلسة، يرجى تسجيل الدخول مجدداً"
Forbidden:         "ليس لديك صلاحيات للوصول لهذا المورد"
NotFound:          "البيانات غير موجودة"
BadRequest:        "[Dynamic user message]"
ServerError:       "خطأ في الخادم: 500"
ServiceUnavailable:"الخدمة غير متاحة حالياً"
DecodingError:     "فشل في قراءة البيانات"
Unknown:           "حدث خطأ غير متوقع"
```

---

## 🚨 Common Pitfalls

### ❌ DON'T: Access data directly without checking result type
```kotlin
val profile = userRepository.getUserProfile().data  // CRASH if Failure!
```

### ✅ DO: Check result type first
```kotlin
val result = userRepository.getUserProfile()
val profile = result.getOrNull()
```

### ❌ DON'T: Ignore errors
```kotlin
coursesRepository.getEnrolledCourses()  // Ignores errors
```

### ✅ DO: Handle appropriately
```kotlin
when (val result = coursesRepository.getEnrolledCourses()) {
    is AppResult.Success -> displayCourses(result.data)
    is AppResult.Failure -> showError(result.error.errorDescription)
    is AppResult.Loading -> showLoadingBar()
}
```

---

## 📚 Further Reading

- **Architecture Guide**: `/app/src/main/java/com/example/moodlegovapp/data/ARCHITECTURE.md`
- **Full RefactoringDetails**: `/DATA_LAYER_REFACTORING_COMPLETE.md`

---

**Last Updated**: June 11, 2026  
**Version**: 1.0  
**Status**: Production Ready ✅

