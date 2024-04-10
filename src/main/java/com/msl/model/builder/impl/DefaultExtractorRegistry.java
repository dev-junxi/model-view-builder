package com.msl.model.builder.impl;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.msl.base.KeyPair;
import com.msl.model.builder.ExtractorRegistry;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.BuildingTemp;
import com.msl.model.utils.StoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.HashMultimap.create;
import static java.util.Collections.*;

/**
 * @author wanglq
 * Date 2022/11/9
 * Time 17:51
 */
@SuppressWarnings("all")
public class DefaultExtractorRegistry implements ExtractorRegistry<DefaultExtractorRegistry> {
    private Logger logger = LoggerFactory.getLogger(DefaultExtractorRegistry.class);
    /**
     * key为model类型,value为通过该类型数据获取指定命名空间下id数据的方式
     * {T.class, [(T t) -> (namespace,ids)]}
     */
    private final SetMultimap<Class<?>, Function<Object, KeyPair<Set<Object>>>> idExtractors = create();
    /**
     * key为model类型，value为通过该类型数据获取指定命名空间下集合数据的方式
     * {T.class, [(T t) -> (namespace,values)]}
     */
    private final SetMultimap<Class<?>, Function<Object, KeyPair<Map<Object, Object>>>> valueExtractors = create();

    /**
     * key为model类型,value为通过该类型数据获取指定命名空间下id数据的方式
     * {T.class, [(T t) -> (namespace,ids)]}
     */
    private final ConcurrentMap<Class<?>, Set<Function<Object, KeyPair<Set<Object>>>>> cachedIdExtractors = new ConcurrentHashMap<>();
    /**
     * key为model类型，value为通过该类型数据获取指定命名空间下集合数据的方式
     * {T.class, [(T t) -> (namespace,values)]}
     */
    private final ConcurrentMap<Class<?>, Set<Function<Object, KeyPair<Map<Object, Object>>>>> cachedValueExtractors = new ConcurrentHashMap<>();

    @Override
    public <E> DefaultExtractorRegistry valueFromSelf(Class<E> type, Function<E, Object> idExtractor) {
        return on(type).value(i -> i).id(idExtractor).to(type);
    }

    @Override
    public <E> DefaultExtractorRegistry extractId(Class<E> type, Function<E, Object> idExtractor, Object toIdNamespace) {
        return on(type).id(idExtractor).to(toIdNamespace);
    }

    @Override
    public <E, V> DefaultExtractorRegistry extractValue(Class<E> type, Function<E, Object> valueExtractor, Function<V, Object> idExtractor, Object toValueNamespace) {
        return on(type).value(valueExtractor).id(idExtractor).to(toValueNamespace);
    }

    /**
     * 判断指定类型是否存在value提取器
     *
     * @param type 指定的类型
     * @return 该类型是否存在value提取器
     */
    @Override
    public boolean existsValueExtractor(Class<?> type) {
        return valueExtractors.containsKey(type);
    }

    /**
     * 获取指定类型下的id提取器
     *
     * @param type 指定的类型
     * @return id提取器集合
     */
    @Override
    public Set<Function<Object, KeyPair<Set<Object>>>> getIdExtractors(Class<?> type) {
        return StoreUtil.getAllSupperTypes(type).stream().filter(idExtractors::containsKey).map(idExtractors::get).flatMap(Set::stream).collect(Collectors.toSet());
    }

    /**
     * @param clazz
     * @return
     */
    @Override
    public Set<Function<Object, KeyPair<Map<Object, Object>>>> getValueExtractors(Class<?> clazz) {
        return StoreUtil.getAllSupperTypes(clazz).stream().filter(valueExtractors::containsKey).map(valueExtractors::get).flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public void doExtract(Object obj, BuildContext buildContext, final BuildingTemp buildingTemp) {
        if (obj == null) {
            return;
        }
        StoreUtil.computeIfAbsent(cachedValueExtractors, obj.getClass(), this::getValueExtractors).forEach(fun -> {
            KeyPair<Map<Object, Object>> values = fun.apply(obj);
            Map<Object, Object> filteredValues = StoreUtil.filterValueMap(values, buildContext);
            buildingTemp.mergeIds(values.getKey(), new HashSet<>(values.getValue().keySet()));
            buildingTemp.mergeValues(values.getKey(), filteredValues);
        });
        StoreUtil.computeIfAbsent(cachedIdExtractors, obj.getClass(), this::getIdExtractors).forEach(fun -> {
            KeyPair<Set<Object>> idsPair = fun.apply(obj);
            buildingTemp.mergeIds(idsPair.getKey(), new HashSet<>(idsPair.getValue()));
        });
    }

    private <E> OnExtracting<E> on(Class<E> type) {
        return new OnExtracting<>(type);
    }

    private final class OnExtracting<E> {
        private final Class<E> objType;

        private OnExtracting(Class<E> objType) {
            this.objType = objType;
        }

        public ExtractingId id(Function<E, Object> idExtractor) {
            return new ExtractingId(idExtractor);
        }

        public ExtractingValue value(Function<E, ?> valueExtractor) {
            return new ExtractingValue(valueExtractor);
        }

        public <V> ExtractingValue value(Function<E, Iterable<V>> valueExtractor, Function<V, Object> idExtractor) {
            return new ExtractingValue(valueExtractor).id(idExtractor);
        }

        public final class ExtractingValue {

            private final Function<E, Object> valueExtractor;
            private Function<Object, Object> idExtractor;

            private ExtractingValue(Function<E, ?> valueExtractor) {
                this.valueExtractor = (Function<E, Object>) valueExtractor;
            }

            public <K> ExtractingValue id(Function<K, Object> idExtractor) {
                this.idExtractor = (Function<Object, Object>) idExtractor;
                return this;
            }

            public DefaultExtractorRegistry to(Object valueNamespace) {
                valueExtractors.put(objType, obj -> {
                    Object rawValue = valueExtractor.apply((E) obj);
                    Map<Object, Object> value;
                    if (rawValue == null) {
                        value = emptyMap();
                    } else {
                        if (idExtractor != null) {
                            if (rawValue instanceof Iterable) {
                                if (rawValue instanceof Collection) {
                                    value = new HashMap<>(((Collection) rawValue).size());
                                } else {
                                    value = new HashMap<>();
                                }
                                for (E e : ((Iterable<E>) rawValue)) {
                                    value.put(idExtractor.apply(e), e);
                                }
                            } else {
                                value = singletonMap(idExtractor.apply(rawValue), rawValue);
                            }
                        } else {
                            if (rawValue instanceof Map) {
                                value = (Map<Object, Object>) rawValue;
                            } else {
                                logger.warn("invalid value extractor for:{}->{}", obj, rawValue);
                                value = emptyMap();
                            }
                        }
                    }
                    return new KeyPair<>(valueNamespace, value);
                });
                cachedValueExtractors.clear();
                return DefaultExtractorRegistry.this;
            }
        }

        public final class ExtractingId {

            private final Function<E, Object> idExtractor;

            private ExtractingId(Function<E, Object> idExtractor) {
                this.idExtractor = idExtractor;
            }

            public DefaultExtractorRegistry to(Object idNamespace) {
                idExtractors.put(objType, obj -> {
                    Object rawId = idExtractor.apply((E) obj);
                    Set<Object> ids;
                    if (rawId == null) {
                        ids = emptySet();
                    } else {
                        if (rawId instanceof Iterable) {
                            ids = Sets.newHashSet((Iterable) rawId);
                        } else {
                            ids = singleton(rawId);
                        }
                    }
                    return new KeyPair<>(idNamespace, ids);
                });
                cachedIdExtractors.clear();
                return DefaultExtractorRegistry.this;
            }
        }
    }
}
