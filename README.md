# json-path-builder
A Java library for building json path and json pointers expressions using code.

JSONPath is defined by https://datatracker.ietf.org/doc/html/rfc9535.
My personal interest in this was because of its use in Redis: https://redis.io/docs/data-types/json/path/

This implementation is currently incomplete (lacks support for a number of constructs)

`JsonPath.parse()` reads the paths that the builder can represent: the root identifier followed by
child segments that select a single member name, array index or wildcard, in either bracket or
shorthand notation. Descendant segments, slices, filters and segments with more than one selector
are rejected with a `ParseException`.

Json pointers are defined by https://datatracker.ietf.org/doc/html/rfc6901. Support for those was added later
as I kept using this library in my other code.