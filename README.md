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
For querying for a specific course, subject, and more!

Accepted Fields: 
* subject (Ex: `["CSE"]`, `["BIO"]`, `["CSE", "BIO"]`)
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
For querying when you know the CRN of your desired course. 

Accepted Fields:
* crn (Ex: `12384`) **mandatory**
* term: (Ex: `"202620"`) **mandatory**

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

### getScheduleByCourses
For querying for around 10 of the best possible schedules for your desired courses.

Accepted Fields:
* courses (Ex: `["CSE 374"]`, `["CSE 374", "CSE 381"]`) **mandatory**
* campus (Ex: `["O"]` (for Oxford), `["H"]` (for Hamilton), `["O","H"]` (for both Oxford and Hamilton)) **mandatory**
* term (Ex: `"202620"`) **mandatory**
* optimizeFreeTime (Ex: `false`, `true`)
* preferredStart (Ex: `"10:00am"`, `"12:00am"`)
* preferredEnd (Ex: `"4:30pm"`, `"11:59pm"`)

```bash
query {
  getScheduleByCourses(
    courses: [
      "CSE 374", 
      "CSE 381",
      "CSE 383",
      "STC 135",
      "BIO 115",
      "SLM 150C"
    ]
    campus: ["O"]
    term: "202620"
    optimizeFreeTime: true
    preferredStart: "10:00am"
    preferredEnd: "4:30pm"
  ) {
    ... on SuccessSchedule {
      schedules {
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
        freeTime
      }
    }
    ... on ErrorSchedule {
      error
      message
    }
  }
}
```
This request is querying for a schedule of the following classes: `CSE 374`, `CSE 381`, `CSE 383`, `STC 135`, `BIO 115`, `SLM 150C` (my Spring 2026 schedule!) at the `Oxford` campus during the `Spring 2026` term, optimizing based on free time, with the full schedule starting from `10:00am` to `4:30pm`.

### getFillerByAttributes
For querying for a modified schedule that satisfies your credit requirements!

Accepted Fields:
* attributes (Ex: `["PA1C"]`, `["PAIC","PA3A"]`) **mandatory**
* courses (Ex: `["CSE 374"]`, `["CSE 374", "CSE 381"]`) **mandatory**
* campus (Ex: `["O"]` (for Oxford), `["H"]` (for Hamilton), `["O","H"]` (for both Oxford and Hamilton)) **mandatory**
* term (Ex: `"202620"`) **mandatory**
* preferredStart (Ex: `"10:00am"`, `"12:00am"`)
* preferredEnd (Ex: `"4:30pm"`, `"11:59pm"`)

```bash
query {
  getFillerByAttributes(
    attributes: ["PA1C"]
    courses: [
      "CSE 374", 
      "CSE 381",
      "CSE 383",
      "STC 135",
      "BIO 115",
      "SLM 150C"
    ]
    campus: ["O"]
    term: "202620"
    preferredStart: "10:00am"
    preferredEnd: "4:30pm"
  ) {
    ... on SuccessSchedule {
      schedules {
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
        freeTime
      }
    }
    ... on ErrorSchedule {
      error
      message
    }
  }
}
```
This request is querying in hopes of filling any empty slot in the inputted schedule with an `Advanced Writing` class. The `campus`, `term`, `preferredStart`, and `preferredEnd` fields are the same as the `getScheduleByCourses` example. 

### getTerms
For querying for all possible terms and staying up to date!

```bash
query {
  getTerms {
    ... on SuccessField {
      fields {
        name
      }
    }
    ... on ErrorField {
      error
      message
    }
  }
}
```
This request is querying for every possible term name. This will give every name from `"202630"` (`Summer 2026`) to `"201810"` (`Fall 2018`)!
