# Mission: Spring Boot RequestBodyAdvice internals

## Why
Understand exactly how Spring MVC reads request bodies so you can safely implement cross-cutting logging, pre-auth checks, and validation without putting the logic in the wrong layer.

## Success looks like
- Explain the `@RequestBody` pipeline from handler adapter to `HttpMessageConverter`, including where `RequestBodyAdvice` runs.
- Choose between `RequestBodyAdvice`, filters/interceptors, and bean validation for a cross-cutting requirement.
- Implement a `RequestBodyAdvice` that wraps raw input or post-processes deserialized objects safely.

## Constraints
- Focus on Spring Boot / Spring MVC server-side request handling.
- Prioritize internals first, then practical extension points.

## Out of scope
- WebFlux request handling
- `ResponseBodyAdvice` and response serialization details
- Client-side `RestTemplate` / `WebClient` message conversion
