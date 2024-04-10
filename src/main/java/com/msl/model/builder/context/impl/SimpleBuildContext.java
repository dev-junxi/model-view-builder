package com.msl.model.builder.context.impl;

import com.msl.model.builder.LazyBuilderHolder;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.LazyBuildContext;
import com.msl.model.utils.MergeUtil;
import com.msl.model.utils.StoreUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/4
 * Time 17:03
 */
@SuppressWarnings("all")
public class SimpleBuildContext implements LazyBuildContext {
    /**
     * 缓存数据
     * key为命名空间,value为空间集合（map）数据
     */
    private final ConcurrentMap<Object, Map<Object, Object>> cachedData;
    /**
     * 缓存id数据
     * key为命名空间,value为空间内所有id数据
     */
    private final ConcurrentMap<Object, Set<Object>> ids = new ConcurrentHashMap<>();
    /**
     * 延迟构造器中心
     */
    private LazyBuilderHolder lazyBuilderHolder;

    public SimpleBuildContext() {
        this(new ConcurrentHashMap<>());
    }

    public SimpleBuildContext(ConcurrentMap<Object, Map<Object, Object>> cachedData) {
        this.cachedData = cachedData;
    }


    /**
     * 根据命名空间获取空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @return 指定的空间集合数据
     */
    @Override
    public <K, V> Map<K, V> getData(Object namespace) {
        doLazyBuild(namespace);
        return getCachedData(namespace);
    }

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @return 指定数据
     */
    @Override
    public <K, V> V getData(Object namespace, K key) {
        V value = (V) getCachedData(namespace, key);
        if (value != null) {
            return value;
        }
        doLazyBuild(namespace);
        return getCachedData(namespace, key);
    }

    /**
     * 执行延迟构造
     *
     * @param namespace 命名空间
     */
    private void doLazyBuild(Object namespace) {
        Optional.ofNullable(lazyBuilderHolder.getLazyBuilder(namespace))
                //该命名空间存在延迟构造器时，以延迟构造器完成一次数据构造
                .ifPresent(builder -> builder.apply(this));
    }

    /**
     * 根据命名空间获取已缓存的空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @return 指定的空间的已缓存的集合数据
     */
    @Override
    public <K, V> Map<K, V> getCachedData(Object namespace) {
        return (Map<K, V>) StoreUtil.computeIfAbsent(cachedData, namespace, n -> new ConcurrentHashMap<>(8));
    }

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @return 指定数据
     */
    @Override
    public <K, V> V getCachedData(Object namespace, K key) {
        return (V) StoreUtil.computeIfAbsent(cachedData, namespace, n -> new ConcurrentHashMap<>(8)).get(key);
    }

    /**
     * 根据命名空间获取空间内的所有id
     *
     * @param namespace 命名空间
     * @return 指定空间内的所有id
     */
    @Override
    public <K> Set<K> getIds(Object namespace) {
        return (Set<K>) StoreUtil.computeIfAbsent(ids, namespace, n -> new CopyOnWriteArraySet<>());
    }

    /**
     * 合并其他上下文的数据到当前上下文
     *
     * @param otherContext 其他上下文
     */
    @Override
    public void merge(BuildContext otherContext) {
        if (otherContext instanceof SimpleBuildContext) {
            SimpleBuildContext other = (SimpleBuildContext) otherContext;
            other.cachedData.forEach((namespace, values) -> cachedData.merge(namespace, values, MergeUtil::merge));
            other.ids.forEach((ns, ids) -> this.ids.merge(ns, ids, MergeUtil::merge));
            if (this.lazyBuilderHolder == null) {
                this.lazyBuilderHolder = other.lazyBuilderHolder;
            } else if (other.lazyBuilderHolder != null) {
                this.lazyBuilderHolder = mergeHolder(this.lazyBuilderHolder, other.lazyBuilderHolder);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 合并lazyHolder
     *
     * @param holder1
     * @param holder2
     * @return
     */
    private LazyBuilderHolder mergeHolder(LazyBuilderHolder holder1, LazyBuilderHolder holder2) {
        final Map<Object, Function<LazyBuildContext, Map<Object, Object>>> builders = new HashMap<>(this.lazyBuilderHolder.getLazyBuilders());
        holder2.getLazyBuilders().forEach(builders::putIfAbsent);
        return new LazyBuilderHolder() {
            private final Map<Object, Function<LazyBuildContext, Map<Object, Object>>> lazyBuilders = builders;

            @Override
            public Map<Object, Function<LazyBuildContext, Map<Object, Object>>> getLazyBuilders() {
                return builders;
            }

            @Override
            public Function<LazyBuildContext, Map<Object, Object>> getLazyBuilder(Object valueNamespace) {
                return builders.get(valueNamespace);
            }
        };
    }

    /**
     * 设置延迟构造器
     *
     * @param lazyBuilderHolder 延迟构造器执有者
     */
    @Override
    public void setupLazyBuilderHolder(LazyBuilderHolder lazyBuilderHolder) {
        this.lazyBuilderHolder = lazyBuilderHolder;
    }

}
