package com.msl.model.builder;

import com.msl.base.KeyPair;
import com.msl.model.builder.context.BuildContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 11:54
 */
public interface BuilderRegistry<T extends BuilderRegistry<?>> {
    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, Function, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @param <K>          id类型
     * @return 返回注册中心
     */
    <K> T buildValue(Object idNamespace,
                     Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder);

    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, BiFunction, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @param <K>          id类型
     * @return 返回注册中心
     */
    <K, B extends BuildContext> T buildValue(Object idNamespace,
                                             BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder);

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @param <K>              id类型
     * @return 返回注册中心
     */
    <K> T buildValue(Object idNamespace,
                     Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace);

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @param <K>              id类型
     * @return 返回注册中心
     */
    <K, B extends BuildContext> T buildValue(Object idNamespace,
                                             BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace);


    /**
     * 根据id命名空间获取所的构造器
     *
     * @param idNamespace id命名空间
     * @return value空间和对应的构造器
     */
    Set<KeyPair<BiFunction<BuildContext, Collection<Object>, Map<Object, Object>>>> getBuilders(Object idNamespace);
}
