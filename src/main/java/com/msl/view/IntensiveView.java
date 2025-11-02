package com.msl.view;


import com.msl.model.builder.context.BuildContext;
import com.msl.model.utils.HasKey;
import com.msl.view.mapper.ViewMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sujunxi
 */
public class IntensiveView<T extends HasKey<?>> extends View<T> {
    protected ViewMapper viewMapper;

    public IntensiveView(T source, BuildContext context, ViewMapper viewMapper) {
        super(source, context);
        this.viewMapper = viewMapper;
    }

    /**
     * 构造单个view返回
     *
     * @param namespace value命名空间
     * @param key       待构造的value对应的key
     * @param <V>       view类型
     * @param <K>       key类型
     * @param <D>       value类型
     * @return view
     */
    protected <V extends View<D>, K, D> V build(Class<D> namespace, K key) {
        return Optional.ofNullable(key)
                .map(k -> context.getData(namespace, k))
                .map(d -> viewMapper.<D, V>map(d, context))
                .orElse(null);
    }

    /**
     * 构造view集合返回
     *
     * @param namespace value命名空间
     * @param key       待构造的value集合对应的key
     * @param <V>       view类型
     * @param <K>       key类型
     * @param <D>       value类型
     * @return view
     */
    protected <V extends View<D>, K, D> List<V> buildList(Class<D> namespace, K key) {
        return Optional.ofNullable(key)
                .map(k -> context.<K, List<D>>getData(namespace, k))
                .map(list -> list.stream()
                        .map(d -> viewMapper.<D, V>map(d, context))
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

    }

}
