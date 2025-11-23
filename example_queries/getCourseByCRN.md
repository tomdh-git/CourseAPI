# getCourseByCRN
For querying when you know the CRN of your desired course.

Accepted Fields:
* crn (Ex: `12384`) **mandatory**
* term: (Ex: `"202620"`) **mandatory**

```bash
query {
  getCourseByCRN(input:{
    crn: 12384
    term: "202620"
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
This request is querying for courses with the crn `12384` during the `Spring 2026` term.
