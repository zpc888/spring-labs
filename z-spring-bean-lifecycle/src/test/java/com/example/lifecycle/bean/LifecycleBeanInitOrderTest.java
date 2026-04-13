package com.example.lifecycle.bean;

import com.example.lifecycle.BaseLifecycleTest;
import com.example.lifecycle.DependentBeanLifecycleLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LifecycleBeanInitOrderTest extends BaseLifecycleTest {

    @AfterEach
    void tearDown() {
        DependentBeanLifecycleLog.clear();
    }

    @Test
    void testLifecycleBeanInitializationOrder() {
        DependentBeanLifecycleLog.clear();
        
        try (ClassPathXmlApplicationContext context = createContext()) {
            context.getBean("lifecycleBean", LifecycleBean.class);
            
            List<String> logs = DependentBeanLifecycleLog.getOrderLogs();
            
            assertTrue(logs.stream().anyMatch(l -> l.contains("BeanNameAware")), 
                "Should have BeanNameAware callback");
            assertTrue(logs.stream().anyMatch(l -> l.contains("BeanFactoryAware")), 
                "Should have BeanFactoryAware callback");
            assertTrue(logs.stream().anyMatch(l -> l.contains("ApplicationContextAware")), 
                "Should have ApplicationContextAware callback");
            assertTrue(logs.stream().anyMatch(l -> l.contains("postProcessBeforeInitialization")), 
                "Should have BeanPostProcessor.beforeInit");
            assertTrue(logs.stream().anyMatch(l -> l.contains("@PostConstruct")), 
                "Should have @PostConstruct callback");
            assertTrue(logs.stream().anyMatch(l -> l.contains("afterPropertiesSet")), 
                "Should have InitializingBean callback");
            assertTrue(logs.stream().anyMatch(l -> l.contains("customInit")), 
                "Should have custom init-method");
            assertTrue(logs.stream().anyMatch(l -> l.contains("postProcessAfterInitialization")), 
                "Should have BeanPostProcessor.afterInit");
        }
    }
}
