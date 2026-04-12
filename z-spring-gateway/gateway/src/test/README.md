# Gateway Integration Tests

## Random-port cluster test

`GatewayRandomPortsIntegrationTest` starts 3 real JVM processes on random ports:
- 1 gateway instance
- 2 hello-service instances (`dev1` and `dev2`)

It verifies these scenarios through gateway HTTP calls:
- `/app1/**` routes to hello dev1
- `/app2/**` routes to hello dev2
- `/tenant/app2/**` requires `X-Tenant: gold`
- `RequestTiming` filter adds `X-Latency-Ms`
- task CRUD payload forwarding works through gateway
- task data remains isolated between dev1 and dev2 instances

## Run only this suite

```bash
cd /home/zpc/works/study/sandbox/spring-gateway-poc
./gradlew :gateway:test --tests com.example.gateway.GatewayRandomPortsIntegrationTest --no-daemon --console=plain
```

