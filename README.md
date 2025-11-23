# Miami CourseAPI

## Summary
Miami CourseAPI is a SpringBoot GraphQL API built with Gradle. It exposes a GraphQL schema for querying courses and schedules through the `/graphql` endpoint. It accesses the Miami Course List  (https://www.apps.miamioh.edu/courselist) for finding courses and valid fields. 
## Host
CourseAPI is currently being hosted at `https://courseapi-production-3751.up.railway.app`

The graphql endpoint is `https://courseapi-production-3751.up.railway.app/graphql` (POST)

The alive endpoint is `https://courseapi-production-3751.up.railway.app/alive` (GET)

## Requirements
- JDK 17
- Gradle 8+

## Local Build and Run

### 1. Clone the repository
```bash
git clone https://github.com/tomdh-git/CourseAPI.git [location]
cd [location]
```

### 2. Build the project
```bash
./gradlew clean build
```

### 3. Run the application
```bash
./gradlew bootRun
```
Then the application will be hosted at:
```bash
http://localhost:8080/graphql
```

## Example Queries

### getCourseByInfo

### getCourseByCRN

### getScheduleByCourses

### getFillerByAttributes

### getTerms
