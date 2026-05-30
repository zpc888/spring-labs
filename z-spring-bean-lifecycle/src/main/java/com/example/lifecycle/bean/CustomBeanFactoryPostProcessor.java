package com.example.lifecycle.bean;

import com.example.lifecycle.DependentBeanLifecycleLog;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * called earlier before any singleton bean is created, after bean definitions are ready
 */
@Component
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DependentBeanLifecycleLog.log(CustomBeanFactoryPostProcessor.class, "BeanFactoryPostProcessor", "Called after bean definitions being loaded/ready");
    }
}
