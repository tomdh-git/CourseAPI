# getCourseByInfo
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
  getCourseByInfo(input:{
    term: "202620"
    campus: ["O"]
    subject: ["CSE"]
    courseNum: "381"
  } ) {
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