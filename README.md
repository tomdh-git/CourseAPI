# Miami CourseAPI

## Summary
Miami CourseAPI is a SpringBoot GraphQL API built with Gradle. It exposes a GraphQL schema for querying courses and schedules through the `/graphql` endpoint. It accesses the Miami Course List  (https://www.apps.miamioh.edu/courselist) for finding courses and valid fields. 

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
Accepted Fields: 
* subject (Ex: `["CSE"]`, `["BIO"]`, `["CSE","BIO"]`)
* courseNum (Ex: `134`, `150C`)
* campus (Ex: `["O"]` (for Oxford), `["H"]` (for Hamilton), `["O","H"]` (for both Oxford and Hamilton)) **mandatory**
* attributes (Ex: `["PA1C"]`, `["PAIC","PA3A"]`)
* delivery (Ex: `["Face2Face"]`, `["Face2Face","ONLS"]`)
* term (Ex: `"202620"`) **mandatory**
* openWaitlist (Ex: `"open"`)
* crn (Ex: `12384`)
* partOfTerm (Ex: `["R"]`, `["R","X"]`)
* level (Ex: `"GR"`, `"UG"`)
* courseTitle (Ex: `"Beginning Printmaking"`, `"Lasercutting & Digital Design"`)
* daysFilter (Ex: `["U"]`, `["M","T"]`)
* creditHours (Ex: `3`)
* startEndTime (Ex: `["12:00 AM","11:59 PM"]`, `["10:00 AM","4:30 PM"]`)

```bash
query {
  getCourseByInfo(
    term: "202620"
    campus: ["O"]
    subject: ["CSE"]
    courseNum: "381"
  ) {
    ... on SuccessCourse {
      courses {
        subject
        courseNum
        title
        section
        crn
        campus
        credits
        capacity
        requests
        delivery
      }
    }
    ... on ErrorCourse {
      error
      message
    }
  }
}
```
This request is querying for the `CSE 381` courses in the `Oxford` campus for the `Spring 2026` term.

### getCourseByCRN
Accepted Fields:
* crn (Ex: `12384`)
* term: (Ex: `"202620"`)

```bash
query {
  getCourseByCRN(
    crn: 12384
    term: "202620"
  ) {
    ... on SuccessCourse {
      courses {
        subject
        courseNum
        title
        section
        crn
        campus
        credits
        capacity
        requests
        delivery
      }
    }
    ... on ErrorCourse {
      error
      message
    }
  }
}
```
This request is querying for courses with the crn `12384` during the `Spring 2026` term.


