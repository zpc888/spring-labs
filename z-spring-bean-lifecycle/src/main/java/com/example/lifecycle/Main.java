package com.example.lifecycle;

import com.example.lifecycle.bean.ControllerBean;
import com.example.lifecycle.bean.LifecycleBean;
import com.example.lifecycle.bean.ServiceBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Spring Bean Lifecycle Demo ===\n");

        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml")) {
            context.registerShutdownHook();

            System.out.println("\n--- Initializing Beans ---\n");
            
            LifecycleBean lifecycleBean = context.getBean("lifecycleBean", LifecycleBean.class);
            ServiceBean serviceBean = context.getBean("serviceBean", ServiceBean.class);
            ControllerBean controllerBean = context.getBean("controllerBean", ControllerBean.class);

            System.out.println("\n=== Beans Ready for Use ===");
            System.out.println("lifecycleBean message: " + lifecycleBean.getMessage());
            System.out.println("controllerBean has service: " + (controllerBean.getService() != null));

            System.out.println("\n--- Lifecycle Event Log ---\n");
            System.out.println("All lifecycle events logged:");
            for (String log : DependentBeanLifecycleLog.getOrderLogs()) {
                System.out.println("  " + log);
            }

            System.out.println("\n=== Verification ===");
            int callbackCount = DependentBeanLifecycleLog.getOrderLogs().size();
            System.out.println("Total lifecycle callbacks: " + callbackCount);
            System.out.println("Expected: 12+ callbacks");
            System.out.println("Status: " + (callbackCount >= 12 ? "SUCCESS" : "INCOMPLETE"));
        }

        System.out.println("\n=== Application Closed ===");
    }
}
