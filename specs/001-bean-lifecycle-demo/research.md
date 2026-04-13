# Research: Spring Bean Lifecycle Demo

## Phase 0 Findings

### Bean Lifecycle Callback Order (Spring 6.x)

Based on research from Spring Framework documentation and best practices:

**Initialization Order:**
1. Constructor - Bean instantiation
2. Dependency Injection - All @Autowired fields are set
3. BeanNameAware.setBeanName()
4. BeanFactoryAware.setBeanFactory()
5. ApplicationContextAware.setApplicationContext()
6. BeanPostProcessor.postProcessBeforeInitialization()
7. @PostConstruct method
8. InitializingBean.afterPropertiesSet()
9. Custom init-method (defined in XML)
10. BeanPostProcessor.postProcessAfterInitialization()
11. Bean Ready for Use

**Destruction Order:**
1. @PreDestroy method
2. DisposableBean.destroy()
3. Custom destroy-method (defined in XML)

### Aware Interfaces Order

According to Spring Framework documentation:
- BeanNameAware → BeanFactoryAware → ApplicationContextAware
- These are called after dependency injection but before BeanPostProcessor

### Technical Decisions

**Decision**: Use Maven as build tool
- **Rationale**: Maven is well-supported for Spring Framework projects, easy to configure XML dependencies
- **Alternatives considered**: Gradle (also valid, but Maven more common for XML-based configs)

**Decision**: Use Spring Framework 6.1.x (latest stable)
- **Rationale**: Requires JDK 21, supports modern Java features
- **Artifact**: org.springframework:spring-context:6.1.x

**Decision**: Use Log4j2 for logging
- **Rationale**: Standard logging for Spring Framework, good for tracking lifecycle events

**Decision**: Enable annotation processing in XML
- **Rationale**: Need `<context:annotation-config/>` for @PostConstruct/@PreDestroy to work

### Maven Dependencies

```xml
<!-- Spring Context (required) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.x</version>
</dependency>

<!-- JUnit 5 for testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.x</version>
    <scope>test</scope>
</dependency>

<!-- Log4j2 for logging -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.23.x</version>
</dependency>
```

### Support gradle (groovy style) too

### XML Configuration Requirements

Key elements needed:
- `<beans>` root element
- `<context:annotation-config/>` for @PostConstruct/@PreDestroy
- `<bean id="" class="">` with init-method and destroy-method attributes
- Custom BeanPostProcessor registration

### Spring 6.x Migration Notes

From Spring Framework 6.x upgrade guide:
- javax.annotation → jakarta.annotation (for @PostConstruct, @PreDestroy)
- Spring keeps detecting javax equivalents for backward compatibility
- Recommend using jakarta.* packages for new code
