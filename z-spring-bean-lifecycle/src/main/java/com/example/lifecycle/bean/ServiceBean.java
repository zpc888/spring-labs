package com.example.lifecycle.bean;

import com.example.lifecycle.DependentBeanLifecycleLog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ServiceBean implements BeanNameAware, InitializingBean, DisposableBean {

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        DependentBeanLifecycleLog.log(ServiceBean.class, name, "BeanNameAware.setBeanName");
    }

    @PostConstruct
    public void postConstruct() {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "InitializingBean.afterPropertiesSet");
    }

    public void customInit() {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "customInit (XML)");
    }

    @PreDestroy
    public void preDestroy() {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "@PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "DisposableBean.destroy");
    }

    public void customDestroy() {
        DependentBeanLifecycleLog.log(ServiceBean.class, beanName, "customDestroy (XML)");
    }
}
