package com.example.lifecycle;

import com.example.lifecycle.bean.ControllerBean;
import com.example.lifecycle.bean.LifecycleBean;
import com.example.lifecycle.bean.ServiceBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LifecycleIntegrationTest extends BaseLifecycleTest {

    @AfterEach
    void tearDown() {
        DependentBeanLifecycleLog.clear();
    }

    @Test
    void testAllBeansInitializeCorrectly() {
        DependentBeanLifecycleLog.clear();
        
        try (ClassPathXmlApplicationContext context = createContext()) {
            LifecycleBean lifecycleBean = context.getBean("lifecycleBean", LifecycleBean.class);
            ServiceBean serviceBean = context.getBean("serviceBean", ServiceBean.class);
            ControllerBean controllerBean = context.getBean("controllerBean", ControllerBean.class);
            
            assertNotNull(lifecycleBean);
            assertNotNull(serviceBean);
            assertNotNull(controllerBean);
            
            assertNotNull(controllerBean.getService(), "ControllerBean should have service dependency");
            assertEquals(serviceBean, controllerBean.getService());
            
            List<String> logs = DependentBeanLifecycleLog.getOrderLogs();
            assertTrue(logs.size() >= 20, "Should have at least 20 lifecycle callbacks for 3 beans");
            
            assertTrue(logs.stream().anyMatch(l -> l.contains("lifecycleBean")), "Should have lifecycleBean events");
            assertTrue(logs.stream().anyMatch(l -> l.contains("serviceBean")), "Should have serviceBean events");
            assertTrue(logs.stream().anyMatch(l -> l.contains("controllerBean")), "Should have controllerBean events");
        }
    }
}
