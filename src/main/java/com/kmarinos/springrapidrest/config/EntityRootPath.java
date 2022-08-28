package com.kmarinos.springrapidrest.config;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityRootPath {
    String value() default "";
}
