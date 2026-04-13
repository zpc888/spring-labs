package com.example.lifecycle.bean;

import com.example.lifecycle.DependentBeanLifecycleLog;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

public class CustomBeanPostProcessor implements BeanPostProcessor, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    @NonNull
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        DependentBeanLifecycleLog.log(bean.getClass(), beanName, "BeanPostProcessor.postProcessBeforeInitialization");
        return bean;
    }

    @Override
    @NonNull
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        DependentBeanLifecycleLog.log(bean.getClass(), beanName, "BeanPostProcessor.postProcessAfterInitialization");
        return bean;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
