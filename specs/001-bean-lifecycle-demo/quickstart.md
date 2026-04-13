# Quickstart: Spring Bean Lifecycle Demo

## Prerequisites

- JDK 21 or higher
- Maven 3.8+

## Build

```bash
cd z-spring-bean-lifecycle
mvn clean compile
```

## Run

```bash
mvn exec:java -Dexec.mainClass="com.example.lifecycle.Main"
```

Or run the Main class directly from your IDE.

## Expected Output

Running the application will display lifecycle events in this order:

```
=== Spring Bean Lifecycle Demo ===
1. [Bean Created] - Instance created via constructor
2. [Dependency Injected] - Properties set
3. [BeanNameAware] - setBeanName called: lifecycleBean
4. [BeanFactoryAware] - setBeanFactory called
5. [ApplicationContextAware] - setApplicationContext called
6. [BeanPostProcessor] - postProcessBeforeInitialization: lifecycleBean
7. [@PostConstruct] - postConstruct method called
8. [InitializingBean] - afterPropertiesSet called
9. [Custom Init] - customInit method called
10. [BeanPostProcessor] - postProcessAfterInitialization: lifecycleBean
11. [Bean Ready] - LifecycleBean is now ready to use

=== Using Bean ===
Bean message: Hello from LifecycleBean

=== Shutting Down ===
12. [@PreDestroy] - preDestroy method called
13. [DisposableBean] - destroy method called
14. [Custom Destroy] - customDestroy method called
15. [Bean Destroyed] - LifecycleBean destroyed
```

## Verify Lifecycle Order

The application verifies that:
1. All initialization callbacks are called in correct order
2. All destruction callbacks are called in correct order
3. Aware interfaces are called after dependency injection but before @PostConstruct

## Project Structure

```
z-spring-bean-lifecycle/
├── pom.xml
├── build.gradle
└── src/main/
    ├── java/com/example/lifecycle/
    │   ├── Main.java
    │   └── bean/
    │       ├── LifecycleBean.java
    │       ├── ControllerBean.java
    │       ├── ServiceBean.java
    │       ├── DependentBeanLifecyleLog.java
    │       └── CustomBeanPostProcessor.java
    └── resources/
        └── applicationContext.xml
```

## Troubleshooting

- Ensure JDK 21 is set: `java -version`
- Ensure Maven is available: `mvn -version`
- If @PostConstruct not working, check XML has `<context:annotation-config/>`
