package com.msl.model.builder.annotation;

import java.lang.annotation.*;

/**
 * @author wanglq
 * Date 2022/10/9
 * Time 17:35
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneMatch {
    Class<?>[] value();
}
