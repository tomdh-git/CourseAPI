# getTerms
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