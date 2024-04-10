package com.msl.model.utils;

import com.msl.base.KeyPair;
import com.msl.model.builder.context.BuildContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 09:48
 */
public class StoreUtil {
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> function) {
        //DCL
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    map.put(key, function.apply(key));
                }
            }
        }
        return map.get(key);
    }

    public static <K> Set<K> filterIdSet(Object valueNamespace, Set<K> ids, BuildContext buildContext) {
        Set<Object> cachedDataIds = buildContext.getCachedData(valueNamespace).keySet();
        if (cachedDataIds.isEmpty()) {
            return new HashSet<>(ids);
        }
        return ids.stream()
                .filter(id -> !cachedDataIds.contains(id))
                .collect(Collectors.toSet());

    }

    public static <K> Set<K> filterIdSet(Object valueNamespace, Set<K> ids, BuildContext buildContext, Map<Object, Map<Object, Object>> valuesMap) {
        Set<Object> cachedDataIds = buildContext.getCachedData(valueNamespace).keySet();
        Set<Object> valueMapExistIds = StoreUtil.computeIfAbsent(valuesMap, valueNamespace, i -> new HashMap<>(1)).keySet();
        if (cachedDataIds.isEmpty() && valueMapExistIds.isEmpty()) {
            return new HashSet<>(ids);
        }
        return ids.stream()
                .filter(id -> !cachedDataIds.contains(id) && !valueMapExistIds.contains(id))
                .collect(toSet());

    }

    public static Map<Object, Object> filterValueMap(KeyPair<Map<Object, Object>> values, BuildContext buildContext) {
        Map<Object, Object> cachedData = buildContext.getCachedData(values.getKey());
        if (cachedData.isEmpty()) {
            return new HashMap<>(values.getValue());
        }
        return values.getValue().entrySet().stream()
                .filter(en -> !cachedData.containsKey(en.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 获取所有父类型（class+interface)
     *
     * @return 所有父类型
     */
    public static Set<Class<?>> getAllSupperTypes(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<>();
        if (clazz == null || clazz == Object.class) {
            return classes;
        }
        classes.add(clazz);
        classes.addAll(getAllSupperTypes(clazz.getSuperclass()));
        for (Class<?> superInterface : clazz.getInterfaces()) {
            classes.addAll(getAllSupperTypes(superInterface));
        }
        return classes;
    }

    /**
     * 合并数据到上下文
     *
     * @param valuesMap    待合并数据{命名空间，数据集合}
     * @param buildContext 上下文
     */
    public static void mergeValueToBuildContext(Map<Object, Map<Object, Object>> valuesMap, BuildContext buildContext) {
        valuesMap.forEach((k, v) -> buildContext.getCachedData(k).putAll(v));
    }

    /**
     * 合并数据到上下文
     *
     * @param valueMap       待合并数据
     * @param valueNamespace 命名空间
     * @param buildContext   上下文
     */
    public static void mergeValueToBuildContext(Map<Object, Object> valueMap, Object valueNamespace, BuildContext buildContext) {
        buildContext.getCachedData(valueNamespace).putAll(valueMap);
    }

}
