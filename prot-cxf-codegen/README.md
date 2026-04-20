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
configKey: pingClient

staticHeaders:
  - name: X-Tenant
    value: demo

dynamicHeaders:
  - com.example.headers.AuthHeaderProvider

operations:
  pingOperation:
    action: PingAction
```

Generated SEI behavior:
- Always emits `@a.b.RegisteredSoapClient("...")` (fallback: portType name)
- Conditionally emits `@a.b2.StaticHeaders` / `@a.b2.DynamicHeaders`
- Always emits per-method `@a.b3.SoapAction("...")` (fallback: method name)

Credit to  https://github.com/valmol/samples-cxf-codegen-plugin.git 
Inspired by https://github.com/playframework/play-soap#wsdl2java

