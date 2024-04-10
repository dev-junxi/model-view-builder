package com.msl.model.builder;

import com.msl.base.KeyPair;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.BuildingTemp;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 11:42
 */
public interface ExtractorRegistry<T extends ExtractorRegistry<?>> {

    /**
     * 注册提取类型本身的value提取器
     *
     * @param type        类型 Class
     * @param idExtractor Id提取器
     * @param <E>         类型
     * @return
     */
    <E> T valueFromSelf(Class<E> type, Function<E, Object> idExtractor);

    /**
     * 注册指定Model类型的id提取器
     *
     * @param type Model类型 Class
     * @param <E>  Model类型
     * @return 提取器注册中心
     */
    <E> T extractId(Class<E> type, Function<E, Object> idExtractor, Object toIdNamespace);

    /**
     * 注册指定Model类型的value提取器
     *
     * @param type             Model类型
     * @param valueExtractor   value提取器
     * @param idExtractor      提取出的value的id提取器
     * @param toValueNamespace 提取出的value要放入的命名空间
     * @param <E>              Model类型
     * @param <V>              提取出的value的类型
     * @return 提取器注册中心
     */
    <E, V> T extractValue(Class<E> type, Function<E, Object> valueExtractor, Function<V, Object> idExtractor, Object toValueNamespace);

    /**
     * 判断指定类型是否存在value提取器
     *
     * @param type 指定的类型
     * @return 该类型是否存在value提取器
     */
    boolean existsValueExtractor(Class<?> type);

    /**
     * 获取指定类型下的id提取器
     *
     * @param type 指定的类型
     * @return id提取器集合
     */
    Set<Function<Object, KeyPair<Set<Object>>>> getIdExtractors(Class<?> type);

    /**
     * 获取指定类型下的value提取器
     *
     * @param type 指定的类型
     * @return value类型
     */
    Set<Function<Object, KeyPair<Map<Object, Object>>>> getValueExtractors(Class<?> type);

    /**
     * 执行数据提取
     *
     * @param obj          待提取的原始数据
     * @param buildContext 构造上下文
     * @param buildingTemp 构造临时数据
     */
    void doExtract(Object obj, BuildContext buildContext, BuildingTemp buildingTemp);
}
