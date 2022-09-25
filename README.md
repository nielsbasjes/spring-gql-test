# Introduction

This is an experimental project.

I want to figure out how to generate the schema and all related wiring in Java code.

# Current status
Works like a charm.

The base schema is only
```graphql
# The base of the application.
type Query {
    # A greeting is returned
    greeting: String
}
```

The final schema allows you to do
```graphql
query {
    greeting
    sayHello
    getVersion(thing: "S") {
        url
        commit
    }
}
```

which in this case returns

```json
{
  "data": {
    "greeting": "Hello World",
    "sayHello": "Just say hello",
    "getVersion": {
      "url": "Fetch: url()",
      "commit": "Fetch: commit()"
    }
  }
}
```


# References
Thanks to Brad Baker https://github.com/bbakerman :
- https://github.com/spring-projects/spring-graphql/issues/452#issuecomment-1256798212
- https://www.graphql-java.com/documentation/schema/#changing-schema
