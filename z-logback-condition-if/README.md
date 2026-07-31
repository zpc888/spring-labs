# z-logback-condition-if

This sub-project verifies latest Logback conditional behavior with:

- plain Logback startup,
- Spring Boot managed logging startup using `logback-spring.xml`,
- Spring Boot startup with `org.springframework.boot.logging.LoggingSystem=none` so Logback self-initializes from `logback.xml`.

## Versions under test

- Spring Boot `4.1.0`
- Logback `1.5.35`

## What the tests verify

`LogbackConditionIfTest` proves these behaviors:

1. A **top-level** `<condition>` + `<if>/<then>/<else>` configuration works in both plain Logback and Spring Boot and can route logging to `file1.log` instead of `file2.log`.
2. A nested `<if>` **inside an `<appender>`** is reported as invalid by Logback with warnings from `IfNestedWithinSecondPhaseElementSC`.
3. Even with that warning, plain Logback startup using classpath `logback.xml` still starts and writes to `file1.log`.
4. Spring Boot managed startup using classpath `logback-spring.xml` also still starts and writes to `file1.log` in this minimal repro.
5. Spring Boot with `org.springframework.boot.logging.LoggingSystem=none` and classpath `logback.xml` also starts and writes to `file1.log`.

## Observed warning

The nested case reports this warning:

```text
<if> elements cannot be nested within an <appender>, <logger> or <root> element
```

## Takeaway

In this repro, latest Logback clearly marks nested `<if>` inside `<appender>` as unsupported, but it does **not** stop startup by itself. The configuration still resolves the nested branch and writes to the selected file while emitting warnings. That means if a larger Spring Boot application fails to start, the failure is likely influenced by additional configuration or Boot-specific features beyond this minimal nested-`if` case alone.
