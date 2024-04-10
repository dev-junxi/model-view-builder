package com.msl.view.mapper.impl;

import com.msl.model.builder.context.BuildContext;
import com.msl.model.utils.StoreUtil;
import com.msl.view.mapper.ViewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 18:16
 */
@SuppressWarnings("all")
public class DefaultViewMapperImpl implements ViewMapper {
    private Logger logger = LoggerFactory.getLogger(DefaultViewMapperImpl.class);
    private final Map<Class<?>, BiFunction<?, ?, ?>> mappers = new HashMap<>();
    private final ConcurrentMap<Class<?>, BiFunction<?, ?, ?>> modelTypeCache = new ConcurrentHashMap<>();

    /**
     * 映射结果
     *
     * @param model        原模型数据
     * @param buildContext 上下文
     * @return View结果
     */
    @Override
    public <M, V> V map(M model, BuildContext buildContext) {
        BiFunction mapper = getMapper(model.getClass());
        if (mapper == null) {
            throw new NoSuchElementException("不支持model<" + model.getClass().getSimpleName() + ">对应View的映射");
        }
        return (V) mapper.apply(buildContext, model);
    }

    private BiFunction getMapper(Class<?> modelType) {
        return modelTypeCache.computeIfAbsent(modelType, t -> {
            BiFunction<?, ?, ?> result = mappers.get(t);
            if (result == null) {
                for (Class<?> c : StoreUtil.getAllSupperTypes(t)) {
                    result = mappers.get(c);
                    if (result != null) {
                        return result;
                    }
                }
            }
            if (result == null) {
                logger.warn("cannot found model's view:{}", modelType);
            }
            return result;
        });
    }

    /**
     * <p>addMapper.</p>
     *
     * @param modelType   模型数据
     * @param viewFactory 映射关系
     * @param <M>         模型类型
     * @param <V>         View类型
     * @return
     */
    public <M, V> DefaultViewMapperImpl addMapper(Class<M> modelType,
                                                  BiFunction<BuildContext, M, V> viewFactory) {
        mappers.put(modelType, viewFactory);
        return this;
    }
}
