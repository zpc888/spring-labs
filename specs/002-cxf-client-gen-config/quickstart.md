# Quickstart: CXF Client Generation Config Plugin

## Prerequisites

- Java 21+
- Maven 3.6+
- Apache CXF 4.2.0

## Quick Start

### 1. Add Dependency

Add SnakeYAML to your pom.xml:

```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.2</version>
</dependency>
```

### 2. Create Configuration File

Create a `client-gen-config.yaml` file:

```yaml
configKey: pingClient

staticHeaders:
  - name: X-Tenant
    value: demo
    ifExisting: true

dynamicHeaders:
  - com.example.soap.header.AuthHeaderProvider

operations:
  ping:
    action: pingAction
  echo:
    action: ""
```

### 3. Use with Maven

In your pom.xml, configure the CXF codegen plugin:

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <version>${cxf.version}</version>
    <executions>
        <execution>
            <id>generate-sources</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
            <configuration>
                <sourceRoot>${project.build.directory}/generated-sources</sourceRoot>
                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>${basedir}/src/main/resources/your-service.wsdl</wsdl>
                        <extraargs>
                            <extraarg>-client-gen-config</extraarg>
                            <extraarg>client-gen-config.yaml</extraarg>
                        </extraargs>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 4. Run Generation

```bash
mvn generate-sources
```

The generated SEI interface will include:
- mandatory `@a.b.RegisteredSoapClient("...")`
- optional `@a.b2.StaticHeaders` / `@a.b2.DynamicHeaders`
- mandatory method-level `@a.b3.SoapAction("...")`

## Configuration File Locations

The config file can be specified as:

1. **File path**: `src/main/resources/my-config.yaml`
2. **Classpath**: `my-config.yaml` (must be in src/main/resources)

The tool will first try to load from file path, then fall back to classpath.

## Examples

### Example: Basic Usage

See `prot-cxf-codegen/prot-cxf-codegen-soap-client/src/main/resources/client-gen-config.yaml`

### Example: Multiple Operations

```yaml
configKey: bookClient
operations:
  ping:
    action: pingAction
  addBook:
    action: ""
```

## Building the Plugin

```bash
cd prot-cxf-codegen
mvn clean install
```

## Running Tests

```bash
mvn test -Dtest=ClientGenConfigTest
```

## Troubleshooting

### Config File Not Found

Ensure the YAML file exists at the specified path or is on the classpath.

### Annotations Not Applied

- Verify the annotation classes are on the classpath
- Check that operation names match the WSDL operation names exactly