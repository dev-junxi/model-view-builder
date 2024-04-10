package com.msl.model.builder;

import com.msl.model.builder.context.LazyBuildContext;
import com.msl.model.builder.impl.DefaultBuilderRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 11:54
 */
public interface LazyBuilderRegistry<T extends LazyBuilderRegistry<?>> extends BuilderRegistry<T>, LazyBuilderHolder {
    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @param <K>            id类型
     * @return 返回注册中心
     */
    <K> T lazyBuild(Object idNamespace,
                    Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace);

    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @param <K>            id类型
     * @return 返回注册中心
     */
    <K, B extends LazyBuildContext> T lazyBuild(Object idNamespace,
                                                BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace);


    /**
     * 延迟构造器
     *
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    <K, B extends LazyBuildContext> T lazyBuild(Object valueNamespace, Function<B, ? extends Map<K, ?>> valueBuilder);
}
