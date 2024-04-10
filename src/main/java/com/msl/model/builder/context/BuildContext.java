package com.msl.model.builder.context;

import java.util.Map;
import java.util.Set;

/**
 * @author wanglq
 * Date 2022/11/4
 * Time 16:43
 */
public interface BuildContext {
    /**
     * 根据命名空间获取空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定的空间集合数据
     */
    <K, V> Map<K, V> getData(Object namespace);

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定数据
     */
    <K, V> V getData(Object namespace, K key);

    /**
     * 根据命名空间获取空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param type 以model类型为命名空间
     * @param <K>  空间集合数据key类型
     * @param <V>  空间集合数据value类型
     * @return 指定的空间集合数据
     */
    default <K, V> Map<K, V> getData(Class<V> type) {
        return getData((Object) type);
    }

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定数据
     */
    default <K, V> V getData(Class<V> namespace, K key) {
        return getData((Object) namespace, key);
    }

    /**
     * 根据命名空间获取已缓存的空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定的空间的已缓存的集合数据
     */
    <K, V> Map<K, V> getCachedData(Object namespace);

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定数据
     */
    <K, V> V getCachedData(Object namespace, K key);

    /**
     * 根据命名空间获取已缓存的空间集合数据
     * 各空间数据以Map形式返回
     *
     * @param type 以model类型为命名空间
     * @param <K>  空间集合数据key类型
     * @param <V>  空间集合数据value类型
     * @return 指定的空间集合数据
     */
    default <K, V> Map<K, V> getCachedData(Class<V> type) {
        return getCachedData((Object) type);
    }

    /**
     * 根据命名空间获取空间内指定id的数据
     * 各空间数据以Map形式返回
     *
     * @param namespace 命名空间
     * @param key       数据id
     * @param <K>       空间集合数据key类型
     * @param <V>       空间集合数据value类型
     * @return 指定数据
     */
    default <K, V> V getCachedData(Class<V> namespace, K key) {
        return getCachedData((Object) namespace, key);
    }


    /**
     * 根据命名空间获取空间内的所有id
     *
     * @param namespace 命名空间
     * @param <K>       空间集合数据key类型
     * @return 指定空间内的所有id
     */
    <K> Set<K> getIds(Object namespace);

    /**
     * 根据命名空间获取空间内的所有id
     *
     * @param type 以model类型为命名空间
     * @param <K>  空间集合数据key类型
     * @return 指定空间内的所有id
     */
    default <K> Set<K> getIds(Class<?> type) {
        return getIds((Object) type);
    }

    /**
     * 合并其他上下文的数据到当前上下文
     *
     * @param otherContext 其他上下文
     */
    void merge(BuildContext otherContext);
}
