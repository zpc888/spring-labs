package com.example.lifecycle.bean;

import com.example.lifecycle.DependentBeanLifecycleLog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

public class ControllerBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean {

    private String beanName;
    private ServiceBean service;

    public ControllerBean() {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "Constructor Method");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        DependentBeanLifecycleLog.log(ControllerBean.class, name, "BeanNameAware.setBeanName");
    }

    @Override
    public void setBeanFactory(org.springframework.beans.factory.BeanFactory beanFactory) {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "BeanFactoryAware.setBeanFactory");
    }

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "ApplicationContextAware.setApplicationContext");
    }

    @PostConstruct
    public void postConstruct() {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "InitializingBean.afterPropertiesSet");
    }

    public void customInit() {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "customInit (XML)");
    }

    public void setService(ServiceBean service) {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "inject dependency - service bean");
        this.service = service;
    }

    public ServiceBean getService() {
        return service;
    }

    @PreDestroy
    public void preDestroy() {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "@PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "DisposableBean.destroy");
    }

    public void customDestroy() {
        DependentBeanLifecycleLog.log(ControllerBean.class, beanName, "customDestroy (XML)");
    }
}
