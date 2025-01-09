# json-path-builder
A Java library for building json path and json pointers expressions using code.

The JSONPath is a de-facto standard defined by https://goessner.net/articles/JsonPath/index.html.
My personal interest in this was because of its use in Redis: https://redis.io/docs/data-types/json/path/

This implementation is currently incomplete (lacks support for a number of constructs)

I generally find the standard to be quite loose in definition, and largely
based on examples, without a former grammar and/or spec, but it's quite 
possible that I wasn't able to find it properly.

Json pointers are defined by https://datatracker.ietf.org/doc/html/rfc6901. Support for those was added later
as I kept using this library in my other code.