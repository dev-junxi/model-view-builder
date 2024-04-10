package com.msl.model.builder.impl;

import com.google.common.collect.SetMultimap;
import com.msl.base.KeyPair;
import com.msl.model.builder.LazyBuilderRegistry;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.LazyBuildContext;
import com.msl.model.utils.StoreUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.collect.HashMultimap.create;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 13:22
 */
@SuppressWarnings("all")
public class DefaultBuilderRegistry implements LazyBuilderRegistry<DefaultBuilderRegistry> {
    /**
     * key为id命名空间，value为指定命名空间下通过key中的id命名中间中的ids生成values的方式
     * {idNamespace, [{valueNamespace, ids -> values}]}
     */
    private final SetMultimap<Object, KeyPair<BiFunction<BuildContext, Collection<Object>, Map<Object, Object>>>> valueBuilders = create();
    /**
     * key为value命名空间，value为该命名空间的延迟构造器
     * {valueNamespace, context -> values}
     */
    private final Map<Object, Function<LazyBuildContext, Map<Object, Object>>> lazyBuilders = new HashMap<>();


    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, Function, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @return 返回注册中心
     */
    @Override
    public <K> DefaultBuilderRegistry buildValue(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder) {
        BiFunction<BuildContext, Collection<K>, Map<K, ?>> builder = (context, ids) -> valueBuilder.apply((Collection<K>) ids);
        return buildValue(idNamespace, builder);
    }

    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, BiFunction, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @return 返回注册中心
     */
    @Override
    public <K, B extends BuildContext> DefaultBuilderRegistry buildValue(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder) {
        return buildValue(idNamespace, valueBuilder, idNamespace);
    }

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public <K> DefaultBuilderRegistry buildValue(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace) {
        BiFunction<BuildContext, Collection<K>, Map<K, ?>> builder = (context, ids) -> valueBuilder.apply((Collection<K>) ids);
        return buildValue(idNamespace, builder, toValueNamespace);
    }

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public <K, B extends BuildContext> DefaultBuilderRegistry buildValue(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace) {
        valueBuilders.put(idNamespace, new KeyPair(toValueNamespace, valueBuilder));
        return this;
    }

    /**
     * 根据id命名空间获取所的构造器
     *
     * @param idNamespace id命名空间
     * @return value空间和对应的构造器
     */
    @Override
    public Set<KeyPair<BiFunction<BuildContext, Collection<Object>, Map<Object, Object>>>> getBuilders(Object idNamespace) {
        return valueBuilders.get(idNamespace);
    }

    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public <K> DefaultBuilderRegistry lazyBuild(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace) {
        Function<LazyBuildContext, Map<K, ?>> lazyBuilder = (context) -> {
            Map<K, ?> map = valueBuilder.apply(StoreUtil.<K>filterIdSet(valueNamespace, context.getIds(idNamespace), context));
            StoreUtil.mergeValueToBuildContext((Map<Object, Object>) map, valueNamespace, context);
            return map;
        };
        return lazyBuild(valueNamespace, lazyBuilder);
    }

    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public <K, B extends LazyBuildContext> DefaultBuilderRegistry lazyBuild(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace) {
        Function<B, ? extends Map<K, ?>> lazyBuilder = context -> {
            Map<K, ?> map = valueBuilder.apply(context, StoreUtil.<K>filterIdSet(valueNamespace, context.getIds(idNamespace), context));
            StoreUtil.mergeValueToBuildContext((Map<Object, Object>) map, valueNamespace, context);
            return map;
        };
        return lazyBuild(valueNamespace, lazyBuilder);
    }

    /**
     * 延迟构造器
     *
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    public <K, B extends LazyBuildContext> DefaultBuilderRegistry lazyBuild(Object valueNamespace, Function<B, ? extends Map<K, ?>> valueBuilder) {
        lazyBuilders.put(valueNamespace, (Function) valueBuilder);
        return this;
    }

    /**
     * 获取所有的延迟构造器
     *
     * @return 延迟构造器集合
     */
    @Override
    public Map<Object, Function<LazyBuildContext, Map<Object, Object>>> getLazyBuilders() {
        return lazyBuilders;
    }


    /**
     * 获取指定命名空间的延迟构造器
     *
     * @param valueNamespace 命名空间
     * @return 延迟构造器
     */
    @Override
    public Function<LazyBuildContext, Map<Object, Object>> getLazyBuilder(Object valueNamespace) {
        return lazyBuilders.get(valueNamespace);
    }

}
