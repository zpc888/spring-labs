# Prot CXF Codegen Plugin

Apache CXF's wsdl2java support is pluggable. 
There is a META-INF/tools-plugin.xml descriptor that allows you to define custom generators ("frontend profiles"). 
Sample shows howto implement and use Service Endpoint Interface (SEI) generator. 
Custom generator extends the default one, but provides a different velocity template. 
This velocity template is identical to the default one, except that custom annotations via soap-gen.yaml added for generated SEI.

## client-gen-config support

Custom annotations can be injected with `-client-gen-config`:

```xml
<extraargs>
  <extraarg>-fe</extraarg>
  <extraarg>prot-cxf</extraarg>
  <extraarg>-client-gen-config</extraarg>
  <extraarg>client-gen-config.yaml</extraarg>
</extraargs>
```

YAML format:

```yaml
x-config-key: pingClient
x-base-url: https://localhost:8081/mock
x-jaxb-context-paths:
  - com.example.ping

x-static-headers:
  - name: X-Tenant
    value: demo

x-dynamic-headers:
  - com.example.headers.AuthHeaderProvider

x-operations:
  pingOperation:
    action: PingAction
    static-headers:
      - name: X-Op
        value: ping
```

Generated SEI behavior:
- Always emits `@prot.soap.SoapClient(value=...)` (fallback: portType name)
- Conditionally adds `baseUrl`, `jaxbContextPaths`, `staticHeaders`, `dynamicHeaders` on `@prot.soap.SoapClient`
- Always emits `@prot.soap.SoapAction("...")` (fallback: configured action → WSDL soapAction → operation name)
- Emits `@prot.soap.SoapMethodHeader(...)` only when operation static/dynamic headers are configured

Credit to  https://github.com/valmol/samples-cxf-codegen-plugin.git 
Inspired by https://github.com/playframework/play-soap#wsdl2java

