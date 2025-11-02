package com.msl.view.utils;


import com.msl.model.builder.ModelBuilder;
import com.msl.model.builder.context.impl.SimpleBuildContext;
import com.msl.view.mapper.ViewMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author sujunxi
 */
public class BuilderUtils {
    public static <V, T> List<V> buildView(Collection<T> list, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (null == list || list.isEmpty()) {
            return Collections.emptyList();
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildMulti(list, context);
        return viewMapper.map(list, context);
    }



    public static <V, T> V buildView(T t, ModelBuilder modelBuilder, ViewMapper viewMapper) {
        if (t == null) {
            return null;
        }
        SimpleBuildContext context = new SimpleBuildContext();
        modelBuilder.buildSingle(t, context);
        return viewMapper.map(t, context);
    }
}
