package com.example.lifecycle.bean;

import com.example.lifecycle.DependentBeanLifecycleLog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Bean lifecycle will be:
 * <ul>
 *     <li>1. new - constructor method</li>
 *     <li>2. set dependencies</li>
 *     <li>3. interface BeanNameAware.setBeanName(beanName: String): void</li>
 *     <li>4. interface BeanFactoryAware.setBeanFactory(beanFactory: BeanFactory): void</li>
 *     <li>5. interface ApplicationContextAware.setApplicationContext(applicationContext: ApplicationContext): void</li>
 *     <li>6. interface BeanPostProcessor.postProcessBeforeInitialization(bean: Object, beanName: String): Object</li>
 *     <li>7. annotation method @PostConstruct</li>
 *     <li>8. interface InitializingBean.afterPropertiesSet(): void</li>
 *     <li>9. custom init method - via XML bean definition</li>
 *     <li>10. interface BeanPostProcessor.postProcessAfterInitialization(bean: Object, beanName: String): Object</li>
 *     <li>... bean is ready to service the call by its owner via dependency injection ...</li>
 *     <li>11. annotation method @PreDestory</li>
 *     <li>12. interface DisposableBean.destroy(): void</li>
 *     <li>13. custom destroy method - via XML bean definition</li>
 * </ul>
 */
public class LifecycleBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean {

    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private String message;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        DependentBeanLifecycleLog.log(LifecycleBean.class, name, "BeanNameAware.setBeanName");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "BeanFactoryAware.setBeanFactory");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "ApplicationContextAware.setApplicationContext");
    }

    @PostConstruct
    public void postConstruct() {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "InitializingBean.afterPropertiesSet");
    }

    public void customInit() {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "customInit (XML)");
    }

    @PreDestroy
    public void preDestroy() {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "@PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "DisposableBean.destroy");
    }

    public void customDestroy() {
        DependentBeanLifecycleLog.log(LifecycleBean.class, beanName, "customDestroy (XML)");
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
