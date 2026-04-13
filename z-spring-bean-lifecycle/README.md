# Spring Bean Lifecycle Demo

Demonstrates Spring Framework bean lifecycle with XML configuration (not Spring Boot).

## Prerequisites

- JDK 21 or higher
- Maven 3.8+

## Build

```bash
mvn clean compile
```

## Run

```bash
mvn exec:java
```

Or run the `Main` class directly from your IDE.

## Expected Output

Running the application displays all lifecycle callbacks in order:

**Initialization:**
1. Constructor
2. Inject Dependency - via setter
3. BeanNameAware.setBeanName
4. BeanFactoryAware.setBeanFactory
5. ApplicationContextAware.setApplicationContext
6. BeanPostProcessor.postProcessBeforeInitialization
7. @PostConstruct
8. InitializingBean.afterPropertiesSet
9. customInit (XML-defined)
10. BeanPostProcessor.postProcessAfterInitialization

**Destruction (on context close):**
1. @PreDestroy
2. DisposableBean.destroy
3. customDestroy (XML-defined)

## Project Structure

```
z-spring-bean-lifecycle/
├── pom.xml
└── src/main/
    ├── java/com/example/lifecycle/
    │   ├── Main.java
    │   ├── DependentBeanLifecycleLog.java
    │   └── bean/
    │       ├── LifecycleBean.java
    │       ├── ServiceBean.java
    │       ├── ControllerBean.java
    │       └── CustomBeanPostProcessor.java
    └── resources/
        └── applicationContext.xml
```

## Testing

```bash
mvn test
```

Tests verify:
- Initialization callback order
- Aware interface order
- Destruction callback order
- Integration between beans with dependencies
