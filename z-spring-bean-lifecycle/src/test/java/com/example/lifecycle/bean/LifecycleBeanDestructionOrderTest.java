package com.example.lifecycle.bean;

import com.example.lifecycle.BaseLifecycleTest;
import com.example.lifecycle.DependentBeanLifecycleLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LifecycleBeanDestructionOrderTest extends BaseLifecycleTest {

    @AfterEach
    void tearDown() {
        DependentBeanLifecycleLog.clear();
    }

    @Test
    void testLifecycleBeanDestructionOrder() {
        DependentBeanLifecycleLog.clear();
        
        ClassPathXmlApplicationContext context = createContext();
        context.getBean("lifecycleBean", LifecycleBean.class);
        
        context.close();
        
        List<String> logs = DependentBeanLifecycleLog.getOrderLogs();
        
        assertTrue(logs.stream().anyMatch(l -> l.contains("@PreDestroy")), 
            "Should have @PreDestroy callback");
        assertTrue(logs.stream().anyMatch(l -> l.contains("DisposableBean")), 
            "Should have DisposableBean callback");
        assertTrue(logs.stream().anyMatch(l -> l.contains("customDestroy")), 
            "Should have custom destroy-method");
    }
}
