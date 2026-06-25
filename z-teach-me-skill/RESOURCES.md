# Spring Boot RequestBodyAdvice Resources

## Knowledge

- [Javadoc: `RequestBodyAdvice`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/RequestBodyAdvice.html)
  The contract itself: what each hook does, when it runs, and how it is registered.
- [Javadoc: `RequestBodyAdviceAdapter`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/RequestBodyAdviceAdapter.html)
  The practical starting point for implementations; useful for focusing only on `supports` plus the hooks you need.
- [Spring MVC Reference: `@RequestBody`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)
  The official request-body pipeline overview, especially the role of `HttpMessageConverter` and where bean validation happens.
- [Spring MVC Reference: Controller Advice](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html)
  How `@ControllerAdvice` is discovered, scoped, and applied across controllers.
- [Spring Framework source: `AbstractMessageConverterMethodArgumentResolver`](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodArgumentResolver.java)
  The core read path for `@RequestBody`, including converter selection, `beforeBodyRead`, `afterBodyRead`, `handleEmptyBody`, and validation handoff.
- [Spring Framework source: `RequestResponseBodyMethodProcessor`](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/RequestResponseBodyMethodProcessor.java)
  Shows how `@RequestBody` arguments are resolved and that bean validation happens after deserialization.
- [Spring Framework source: `RequestResponseBodyAdviceChain`](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/RequestResponseBodyAdviceChain.java)
  The clearest source for how multiple advice beans are filtered, ordered, and chained together.

## Wisdom (Communities)

- [Stack Overflow: `spring-boot`](https://stackoverflow.com/questions/tagged/spring-boot)
  Use for concrete integration bugs once you can share a minimal reproduction and the exact MVC stack involved.
- [Stack Overflow: `spring-mvc`](https://stackoverflow.com/questions/tagged/spring-mvc)
  Use for framework-internals questions around argument resolvers, controller advice scoping, and message conversion.
