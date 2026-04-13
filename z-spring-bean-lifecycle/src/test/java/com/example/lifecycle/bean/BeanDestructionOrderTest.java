package com.example.lifecycle.bean;

import com.example.lifecycle.BaseLifecycleTest;
import com.example.lifecycle.DependentBeanLifecycleLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeanDestructionOrderTest extends BaseLifecycleTest {

    @AfterEach
    void tearDown() {
        DependentBeanLifecycleLog.clear();
    }

    @Test
    void testDestructionCallbackOrder() {
        DependentBeanLifecycleLog.clear();
        
        ClassPathXmlApplicationContext context = createContext();
        context.getBean("lifecycleBean", LifecycleBean.class);
        
        context.close();
        
        List<String> logs = DependentBeanLifecycleLog.getLogsForBean("lifecycleBean");
        
        int preDestroyIdx = logs.indexOf(logs.stream().filter(l -> l.contains("@PreDestroy")).findFirst().orElse(""));
        int disposableIdx = logs.indexOf(logs.stream().filter(l -> l.contains("DisposableBean")).findFirst().orElse(""));
        int customDestroyIdx = logs.indexOf(logs.stream().filter(l -> l.contains("customDestroy")).findFirst().orElse(""));
        
        assertTrue(preDestroyIdx >= 0, "Should have @PreDestroy callback");
        assertTrue(disposableIdx >= 0, "Should have DisposableBean callback");
        assertTrue(customDestroyIdx >= 0, "Should have custom destroy-method");
        
        assertTrue(preDestroyIdx < disposableIdx, "@PreDestroy should come before DisposableBean");
        assertTrue(disposableIdx < customDestroyIdx, "DisposableBean should come before custom destroy-method");
    }
}
