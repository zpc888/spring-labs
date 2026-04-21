package prot.soap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SoapClient {
    String value() default "";
    String baseUrl() default "";
    String[] jaxbContextPaths() default {};
    /**
     * Static headers as concatenated "name=value" strings
     */
    String[] staticHeaders() default {};
    /**
     * Dynamic header contributor classes
     */
    Class<?>[] dynamicHeaders() default {};
}

