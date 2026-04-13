package com.example.lifecycle.bean;

import com.example.lifecycle.BaseLifecycleTest;
import com.example.lifecycle.DependentBeanLifecycleLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwareCallbackOrderTest extends BaseLifecycleTest {

    @AfterEach
    void tearDown() {
        DependentBeanLifecycleLog.clear();
    }

    @Test
    void testAwareCallbackOrder() {
        DependentBeanLifecycleLog.clear();
        
        try (ClassPathXmlApplicationContext context = createContext()) {
            context.getBean("lifecycleBean", LifecycleBean.class);
            
            List<String> logs = DependentBeanLifecycleLog.getLogsForBean("lifecycleBean");
            
            int beanNameIdx = logs.indexOf(logs.stream().filter(l -> l.contains("BeanNameAware")).findFirst().orElse(""));
            int beanFactoryIdx = logs.indexOf(logs.stream().filter(l -> l.contains("BeanFactoryAware")).findFirst().orElse(""));
            int appContextIdx = logs.indexOf(logs.stream().filter(l -> l.contains("ApplicationContextAware")).findFirst().orElse(""));
            int postConstructIdx = logs.indexOf(logs.stream().filter(l -> l.contains("@PostConstruct")).findFirst().orElse(""));
            
            assertTrue(beanNameIdx >= 0, "Should have BeanNameAware callback");
            assertTrue(beanFactoryIdx >= 0, "Should have BeanFactoryAware callback");
            assertTrue(appContextIdx >= 0, "Should have ApplicationContextAware callback");
            assertTrue(postConstructIdx >= 0, "Should have @PostConstruct callback");
            
            assertTrue(beanNameIdx < beanFactoryIdx, "BeanNameAware should come before BeanFactoryAware");
            assertTrue(beanFactoryIdx < appContextIdx, "BeanFactoryAware should come before ApplicationContextAware");
            assertTrue(appContextIdx < postConstructIdx, "All Aware callbacks should come before @PostConstruct");
        }
    }
}
