# getFillerByAttributes
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
  getFillerByAttributes(input:{
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
This request is querying in hopes of filling any empty slot in the inputted schedule with an `Advanced Writing` class. The `campus`, `term`, `preferredStart`, and `preferredEnd` fields are the same as the `getScheduleByCourses` example. 
