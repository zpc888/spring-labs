package prot.soap;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoapStaticHeader {
    String name();
    String value();
    boolean ifExisting() default false;
}

