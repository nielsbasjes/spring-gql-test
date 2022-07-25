# Introduction

This is an experimental project.

I want to figure out how to generate the schema and all related wiring in Java code.

# Current status
Works partially.

The schema is generated as I expect it.

Yet when running this query:

```graphql
query {
  DoSomething(thing: "foo") {
    commit
    url
  }
}
```

I expect this

```json
{
  "data": {
    "DoSomething": {
      "commit": "Something",
      "url": "Something"
    }
  }
}
```

but I get this:

```json
{
  "data": {
    "DoSomething": null
  }
}
```