package com.example.lifecycle;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BaseLifecycleTest {

    protected ClassPathXmlApplicationContext createContext() {
        return new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    protected void closeContext(ClassPathXmlApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
