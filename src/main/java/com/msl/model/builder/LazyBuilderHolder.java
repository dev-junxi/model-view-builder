package com.msl.model.builder;

import com.msl.model.builder.context.LazyBuildContext;

import java.util.Map;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/10
 * Time 15:12
 */
public interface LazyBuilderHolder {
    /**
     * 获取所有的延迟构造器
     *
     * @return 延迟构造器集合
     */
    Map<Object, Function<LazyBuildContext, Map<Object, Object>>> getLazyBuilders();

    /**
     * 获取指定命名空间的延迟构造器
     *
     * @param valueNamespace 命名空间
     * @return 延迟构造器
     */
    Function<LazyBuildContext, Map<Object, Object>> getLazyBuilder(Object valueNamespace);
}
