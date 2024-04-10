package com.msl.view.mapper;

import com.msl.model.builder.context.BuildContext;
import com.msl.view.mapper.impl.DefaultViewMapperImpl;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 18:13
 */
public interface ViewMapper {
    /**
     * <p>addMapper.</p>
     *
     * @param modelType   模型数据
     * @param viewFactory 映射关系
     * @param <M>         模型类型
     * @param <V>         View类型
     * @return
     */
    <M, V> ViewMapper addMapper(Class<M> modelType,
                                BiFunction<BuildContext, M, V> viewFactory);

    /**
     * 映射结果
     *
     * @param model        原模型数据
     * @param buildContext 上下文
     * @param <M>          模型类型
     * @param <V>          View类型
     * @return View结果
     */
    <M, V> V map(M model, BuildContext buildContext);

    /**
     * 映射结果
     *
     * @param models       原模型数据集合
     * @param buildContext 上下文
     * @param <M>          模型类型
     * @param <V>          View类型
     * @return View结果
     */
    default <M, V> List<V> map(Collection<M> models, BuildContext buildContext) {
        return models.stream().map(i -> this.<M, V>map(i, buildContext)).collect(toList());
    }
}
