package com.msl.view;


import com.msl.model.builder.context.BuildContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wanglq
 * Date 2022/10/9
 * Time 16:42
 */
public abstract class View<T> {
    protected T source;
    protected BuildContext context;

    public View(T source, BuildContext context) {
        this.context = context;
        this.source = source;
    }

    public T getSource() {
        return source;
    }

    public BuildContext getContext() {
        return context;
    }

    protected <V extends View<D>, K, D> V build(Object namespace, K key, BiFunction<D, BuildContext, V> view) {
        return Optional.ofNullable(key)
                .map(k -> context.<K, D>getData(namespace, k))
                .map(d -> view.apply(d, context))
                .orElse(null);
    }

    protected <V extends View<D>, K, D> List<V> buildList(Object namespace, K key, BiFunction<D, BuildContext, V> view) {
        return Optional.ofNullable(key)
                .map(k -> context.<K, List<D>>getData(namespace, k))
                .map(list -> list.stream().map(d -> view.apply(d, context)).collect(Collectors.toList()))
                .orElse(new ArrayList<>());

    }
}
