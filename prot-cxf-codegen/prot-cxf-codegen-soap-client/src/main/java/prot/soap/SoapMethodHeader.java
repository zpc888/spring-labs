package prot.soap;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface SoapMethodHeader {
    /**
     * Static headers as concatenated "name=value" strings
     */
    String[] staticHeaders() default {};

    /**
     * Dynamic header contributor classes
     */
    Class<?>[] dynamicHeaders() default {};
}


