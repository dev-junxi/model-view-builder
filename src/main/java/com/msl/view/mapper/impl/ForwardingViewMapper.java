package com.msl.view.mapper.impl;

import com.msl.model.builder.context.BuildContext;
import com.msl.view.mapper.ViewMapper;

import java.util.function.BiFunction;

/**
 * <p>Abstract ForwardingViewMapper class.</p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public abstract class ForwardingViewMapper implements ViewMapper {

    private final ViewMapper delegate;

    /**
     * <p>Constructor for ForwardingViewMapper.</p>
     *
     * @param delegate a ViewMapper
     */
    protected ForwardingViewMapper(ViewMapper delegate) {
        this.delegate = delegate;
    }

    /**
     * <p>addMapper.</p>
     *
     * @param modelType   模型数据
     * @param viewFactory 映射关系
     * @return
     */
    @Override
    public <M, V> ViewMapper addMapper(Class<M> modelType, BiFunction<BuildContext, M, V> viewFactory) {
        return delegate.addMapper(modelType, viewFactory);
    }

    /**
     * 映射结果
     *
     * @param model        原模型数据
     * @param buildContext 上下文
     * @return View结果
     */
    @Override
    public <M, V> V map(M model, BuildContext buildContext) {
        return delegate.map(model, buildContext);
    }
}
