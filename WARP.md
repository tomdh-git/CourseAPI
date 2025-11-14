# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Build and Run Commands

### Build
```pwsh
./gradlew build
```

### Run the application
```pwsh
./gradlew bootRun
```

### Run all tests
```pwsh
./gradlew test
```

### Run a single test class
```pwsh
./gradlew test --tests CourseResolverTests
```

### Run a single test method
```pwsh
./gradlew test --tests CourseResolverTests.getCourseByInfo*
```

### Lint/Code Quality
No linting tools configured. The project uses standard Kotlin conventions and Spring Boot conventions.

## High-Level Architecture

This is a **Spring Boot GraphQL API** built in Kotlin that queries course data from Miami University's course list system.

### Domain Structure

**Two main domains:**

1. **Courses**: Fetch and filter courses from Miami University by subject, term, campus, attributes, delivery type, and more.
2. **Schedules**: Build non-conflicting course schedules and find complementary courses with specific attributes to fill free time slots.

### Layered Architecture

The codebase follows a **repository-service-resolver pattern** (GraphQL instead of MVC):

- **Models** (`src/main/kotlin/com/example/courseapi/models/`): Data classes representing domain objects
  - `course/`: `Course`, `CourseResult` (sealed interface with `SuccessCourse`/`ErrorCourse`)
  - `schedule/`: `Schedule`, `ScheduleResult` (sealed interface with `SuccessSchedule`/`ErrorSchedule`)
  - `misc/`: `Field`, `FieldResult` for metadata like terms and campuses
  - `input/`: Validation lists for allowed values (e.g., `ValidSubjects`, `ValidCampuses`, `ValidDeliveryTypes`)

- **Repositories** (`src/main/kotlin/com/example/courseapi/repos/`): HTTP clients and data fetching
  - `CourseRepo`: Constructs POST requests to Miami's course list, parses HTML responses via `RequestService` and `ParseService`
  - `ScheduleRepo`: Builds schedules via Cartesian products of courses, detects time conflicts, optimizes free time

- **Services** (`src/main/kotlin/com/example/courseapi/services/`): Business logic and API interaction
  - `CourseService`: Validates input parameters, delegates to `CourseRepo`
  - `ScheduleService`: Validates schedule parameters, delegates to `ScheduleRepo`
  - `RequestService`: Manages HTTP requests and token caching for Miami's course list
  - `ParseService`: Parses HTML using jsoup to extract course data and metadata

- **Resolvers** (`src/main/kotlin/com/example/courseapi/resolvers/`): GraphQL query endpoints
  - `CourseResolver`: Exposes GraphQL queries like `getCourseByInfo()`, `getCourseByCRN()`, `getScheduleByCourses()`, `getFillerByAttributes()`, `getTerms()`
  - All resolvers use a centralized `safeExecute()` helper that catches custom exceptions and wraps them in error result types

- **Exceptions** (`src/main/kotlin/com/example/courseapi/exceptions/`): Domain-specific error handling
  - `APIException`: When Miami's course list API fails
  - `QueryException`: When queries return too many results
  - `ServerBusyException`: When request queue is full (backpressure)

- **Client** (`src/main/kotlin/com/example/courseapi/client/`): HTTP and rate limiting
  - `HttpClientConfig`: Configures Ktor HTTP client with cookies, redirects, timeouts
  - `RequestLimiter`: Rate limiter using semaphore (5 concurrent workers) and queue (50-item backlog) to prevent overwhelming Miami's server

### Key Design Patterns

1. **Sealed Result Types**: All GraphQL queries return sealed interfaces (`CourseResult`, `ScheduleResult`, `FieldResult`) with success/error variants for type-safe error handling.

2. **Request Limiting**: The `RequestLimiter` acts as a gatekeeper, throttling concurrent requests to Miami's server and throwing `ServerBusyException` if the queue is full.

3. **Token Caching**: `RequestService` caches the CSRF token from Miami's course list page for 2 minutes to reduce unnecessary GET requests.

4. **HTML Parsing**: Uses jsoup to extract course data from raw HTML responses (no JSON API available).

5. **Schedule Optimization**: `ScheduleRepo` computes all valid non-conflicting course combinations and can rank them by free time if `optimizeFreeTime=true`.

6. **Centralized Exception Handling**: The `safeExecute()` in `CourseResolver` catches all exceptions, logs them, and wraps them in appropriate error result types—this is critical for GraphQL error consistency.

### Typical Data Flow

1. GraphQL query arrives at `CourseResolver`
2. Resolver calls appropriate `Service` method (e.g., `CourseService.getCourseByInfo()`)
3. Service validates input parameters; if invalid, throws `IllegalArgumentException`
4. Service delegates to `Repository` (e.g., `CourseRepo`)
5. Repository calls `RequestService` to fetch data from Miami's server
6. Repository calls `ParseService` to extract data from HTML
7. Result wrapped in success type and returned through service to resolver
8. `safeExecute()` catches any exceptions and wraps them in error type
9. GraphQL returns sealed result type (success or error)

### Key Files

- `build.gradle.kts`: Defines Spring Boot 3.3.4, Kotlin 2.0.21, GraphQL starter, Ktor client, jsoup
- `src/main/resources/graphql/schema.graphqls`: GraphQL schema defining queries and types
- `CourseapiApplication.kt`: Spring Boot entry point
