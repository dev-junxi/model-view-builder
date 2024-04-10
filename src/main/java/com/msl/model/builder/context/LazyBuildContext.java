package com.msl.model.builder.context;

import com.msl.model.builder.LazyBuilderHolder;

/**
 * @author wanglq
 * Date 2022/11/4
 * Time 17:44
 */
public interface LazyBuildContext extends BuildContext {
    /**
     * 设置延迟构造器
     *
     * @param lazyBuilderHolder 延迟构造器执有者
     */
    void setupLazyBuilderHolder(LazyBuilderHolder lazyBuilderHolder);
}
