package cc.lj1.auth.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthRequired
public @interface AuthAccessControl {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
