package com.msl.view.annotation;

import java.lang.annotation.*;

/**
 * @author wanglq
 * Date 2022/10/9
 * Time 17:35
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewIgnore {

}
