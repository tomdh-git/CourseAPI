# getScheduleByCourses
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
  getScheduleByCourses(input:{
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
  } ) {
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
