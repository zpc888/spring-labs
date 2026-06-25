# Spring Boot 4 with Jackson v2 and v3

`TestApiControllerTest` shows how Spring Boot 4 behaves when the same JSON shape is handled through Jackson 3 and Jackson 2 models in the same application.

## JSON shape verified by the tests

All successful requests and responses use this logical structure:

```json
{
  "stringField": "example",
  "integerField": 123,
  "dateFiled": "yyyy-MM-dd HH:mm:ss"
}
```

Observed behavior:

1. `POST /test-api/annotate-with-v3` succeeds with the custom date string `yyyy-MM-dd HH:mm:ss`.
2. `POST /test-api/annotate-with-v3a` also succeeds with the same JSON contract.
3. For the Jackson 3 endpoints, `intField` is exposed as `integerField`, and `dateFiled` is deserialized from and serialized back to the same formatted string.
4. `POST /test-api/annotate-with-v2` succeeds with the same JSON contract, because the application provides a dedicated Jackson 2 message-converter path for that model.
5. `POST /test-api/annotate-with-v2a` returns `400 Bad Request` when sent the same custom date string, showing that Spring Boot 4 does not automatically honor that Jackson 2 date annotation path through its default request binding.
6. `POST /test-api/annotate-with-v2a` succeeds only when the request body is first produced by the matching mapper for that model, and the response echoes that mapper-produced date representation.

## Practical takeaway

Spring Boot 4 uses Jackson 3 by default, so Jackson 3-annotated models work naturally with the normal MVC `@RequestBody` flow. Jackson 2 models can still coexist, but matching request/response behavior for custom date formatting requires explicit Jackson 2 conversion support instead of relying on Boot 4's default binding pipeline.
