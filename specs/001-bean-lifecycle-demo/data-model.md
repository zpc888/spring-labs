# Data Model: Spring Bean Lifecycle Demo

## Entities

### LifecycleBean

Represents a demo bean that implements all lifecycle callbacks to demonstrate the Spring bean lifecycle.

**Fields:**
- `beanName` (String) - Bean name from BeanNameAware
- `beanFactory` (BeanFactory) - BeanFactory from BeanFactoryAware  
- `applicationContext` (ApplicationContext) - ApplicationContext from ApplicationContextAware
- `message` (String) - Simple message property for dependency injection demo

**Lifecycle Methods:**
- `setBeanName(String name)` - From BeanNameAware
- `setBeanFactory(BeanFactory beanFactory)` - From BeanFactoryAware
- `setApplicationContext(ApplicationContext applicationContext)` - From ApplicationContextAware
- `postConstruct()` - Annotated with @PostConstruct
- `afterPropertiesSet()` - From InitializingBean
- `customInit()` - Custom init-method
- `preDestroy()` - Annotated with @PreDestroy
- `destroy()` - From DisposableBean
- `customDestroy()` - Custom destroy-method

**Relationships:**
- Implements: BeanNameAware, BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean
- Annotated with: @PostConstruct, @PreDestroy


### ControllerBean

Represents a controller bean that implements all lifecycle callbacks (see `LifecycleBean`). Besides print out the lifecycle callback methods, it also append to `DependentBeanLifecycleLog` static List fields

**Fields:** 
- see `LifecycleBean` Fields
- `service` (ServiceBean) - depends on ServiceBean
 
**Lifecycle Methods:** 
- see `LifecycleBean` Lifecycle Methods
 
**Relationships:** 
- see `LifecycleBean` Relationships 
- depend on `ServiceBean`

# ServiceBean

Represents a service bean that implements all lifecycle callbacks (see `LifecycleBean`). Besides print out the lifecycle callback methods, it also append to `DependentBeanLifecycleLog` static List fields

**Fields:**
- see `LifecycleBean` Fields

**Lifecycle Methods:** 
- see `LifecycleBean` Lifecycle Methods

**Relationships:** 
- see `LifecycleBean` Relationships

### CustomBeanPostProcessor

A custom BeanPostProcessor implementation to demonstrate its role in the lifecycle.

**Fields:**
- `order` (int) - Execution order (implements Ordered)

**Methods:**
- `postProcessBeforeInitialization(Object bean, String beanName)` - Called before initialization
- `postProcessAfterInitialization(Object bean, String beanName)` - Called after initialization

**Relationships:**
- Implements: BeanPostProcessor, Ordered

### ApplicationConfig

XML configuration file defining beans and their lifecycle method associations.

**Configuration Elements:**
- `<context:annotation-config/>` - Enable annotation processing
- Bean definitions with init-method and destroy-method attributes
- LifecycleBean bean definition
- CustomBeanPostProcessor bean definition

## Validation Rules

1. All lifecycle methods must be public
2. @PostConstruct and @PreDestroy methods must have no arguments
3. afterPropertiesSet() and destroy() throw Exception
4. Custom init/destroy methods can have any name but must have no arguments
5. Verify `DependentBeanLifecycleLog` static List field containing the right order for both `ControllerBean` and `ServiceBean` lifecycle callback method orders

## State Transitions

**Initialization States:**
```
NEW → CONSTRUCTED → PROPERTY_SET → INITIALIZING → INITIALIZED → READY
```

**Destruction States:**
```
READY → DESTROYING → DESTROYED
```
