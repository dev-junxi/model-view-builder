package com.msl.model.builder;

import com.msl.model.builder.context.BuildContext;

import java.util.Collections;

/**
 * @author wanglq
 * Date 2022/11/4
 * Time 17:48
 */
public interface ModelBuilder extends LazyBuilderRegistry<ModelBuilder>, ExtractorRegistry<ModelBuilder> {
    /**
     * 构造列表数据
     *
     * @param sources      原始数据
     * @param buildContext 构造上下文
     */
    void buildMulti(Iterable<?> sources, BuildContext buildContext);

    /**
     * 构造单个数据
     *
     * @param one          原始数据
     * @param buildContext 构造上下文
     */
    default void buildSingle(Object one, BuildContext buildContext) {
        buildMulti(Collections.singleton(one), buildContext);
    }

    /**
     * 设置冲突时执行的监听器
     *
     * @param listener 监听器
     * @return modelBuilder自身
     */
    ModelBuilder onConflictListener(Runnable listener);
}
