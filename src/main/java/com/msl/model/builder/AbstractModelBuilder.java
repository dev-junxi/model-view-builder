package com.msl.model.builder;

import com.msl.base.KeyPair;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.BuildingTemp;
import com.msl.model.builder.context.LazyBuildContext;
import com.msl.model.builder.impl.DefaultBuilderRegistry;
import com.msl.model.builder.impl.DefaultExtractorRegistry;
import com.msl.model.utils.StoreUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author wanglq
 * Date 2022/11/10
 * Time 13:23
 */
@SuppressWarnings("all")
public abstract class AbstractModelBuilder implements ModelBuilder {
    private final ExtractorRegistry extractorRegistry;
    private final LazyBuilderRegistry builderRegistry;
    private volatile boolean alreadyBuilt = false;
    private Runnable onConflictListener;

    public AbstractModelBuilder() {
        extractorRegistry = new DefaultExtractorRegistry();
        builderRegistry = new DefaultBuilderRegistry();
    }

    public AbstractModelBuilder(ExtractorRegistry extractorRegistry, LazyBuilderRegistry builderRegistry) {
        this.extractorRegistry = extractorRegistry;
        this.builderRegistry = builderRegistry;
    }

    /**
     * 构造列表数据
     *
     * @param sources      原始数据
     * @param buildContext 构造上下文
     */
    @Override
    public final void buildMulti(Iterable<?> sources, BuildContext buildContext) {
        alreadyBuilt = true;
        doBuild(sources, buildContext);
    }

    /**
     * 执行构建
     *
     * @param sources      原始数据
     * @param buildContext 构造上下文
     */
    protected abstract void doBuild(Iterable<?> sources, BuildContext buildContext);

    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, Function, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @return 返回注册中心
     */
    @Override
    public final <K> AbstractModelBuilder buildValue(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder) {
        tryCheckConflict();
        builderRegistry.buildValue(idNamespace, valueBuilder);
        return this;
    }

    /**
     * 注册构造器
     * <p>
     * 相较于此注册方式{@link #buildValue(Object, BiFunction, Object)} 以idNamespace当作valueNamespace
     *
     * @param idNamespace  id命名空间
     * @param valueBuilder 构造器
     * @return 返回注册中心
     */
    @Override
    public final <K, B extends BuildContext> AbstractModelBuilder buildValue(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder) {
        tryCheckConflict();
        builderRegistry.buildValue(idNamespace, valueBuilder);
        return this;
    }

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public final <K> AbstractModelBuilder buildValue(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace) {
        tryCheckConflict();
        builderRegistry.buildValue(idNamespace, valueBuilder, toValueNamespace);
        return this;
    }

    /**
     * 注册构造器
     *
     * @param idNamespace      id命名空间
     * @param valueBuilder     构造器
     * @param toValueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public final <K, B extends BuildContext> AbstractModelBuilder buildValue(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object toValueNamespace) {
        tryCheckConflict();
        builderRegistry.buildValue(idNamespace, valueBuilder, toValueNamespace);
        return this;
    }

    /**
     * 根据id命名空间获取所的构造器
     *
     * @param idNamespace id命名空间
     * @return value空间和对应的构造器
     */
    @Override
    public Set<KeyPair<BiFunction<BuildContext, Collection<Object>, Map<Object, Object>>>> getBuilders(Object idNamespace) {
        return builderRegistry.getBuilders(idNamespace);
    }

    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public final <K> AbstractModelBuilder lazyBuild(Object idNamespace, Function<? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace) {
        Function<LazyBuildContext, Map<K, ?>> lazyBuilder = context -> {
            Map<K, ?> map = valueBuilder.apply(StoreUtil.<K>filterIdSet(valueNamespace, context.getIds(idNamespace), context));
            StoreUtil.mergeValueToBuildContext((Map<Object, Object>) map, valueNamespace, context);
            buildMulti(map.values(), context);
            return map;
        };
        return lazyBuild(valueNamespace, lazyBuilder);
    }

    /**
     * 注册延迟构造器
     *
     * @param idNamespace    id命名空间
     * @param valueBuilder   延迟构造器
     * @param valueNamespace value命名空间
     * @return 返回注册中心
     */
    @Override
    public final <K, B extends LazyBuildContext> AbstractModelBuilder lazyBuild(Object idNamespace, BiFunction<B, ? super Collection<K>, ? extends Map<K, ?>> valueBuilder, Object valueNamespace) {
        Function<B, ? extends Map<K, ?>> lazyBuilder = context -> {
            Map<K, ?> map = valueBuilder.apply(context, StoreUtil.<K>filterIdSet(valueNamespace, context.getIds(idNamespace), context));
            StoreUtil.mergeValueToBuildContext((Map<Object, Object>) map, valueNamespace, context);
            buildMulti(map.values(), context);
            return map;
        };
        return lazyBuild(valueNamespace, lazyBuilder);
    }

    /**
     * 延迟构造器
     *
     * @param valueNamespace value命名空间
     * @param valueBuilder   延迟构造器
     * @return 返回注册中心
     */
    @Override
    public <K, B extends LazyBuildContext> AbstractModelBuilder lazyBuild(Object valueNamespace, Function<B, ? extends Map<K, ?>> valueBuilder) {
        tryCheckConflict();
        builderRegistry.lazyBuild(valueNamespace, valueBuilder);
        return this;
    }

    /**
     * 获取所有的延迟构造器
     *
     * @return 延迟构造器集合
     */
    @Override
    public Map<Object, Function<LazyBuildContext, Map<Object, Object>>> getLazyBuilders() {
        return builderRegistry.getLazyBuilders();
    }

    /**
     * 获取指定命名空间的延迟构造器
     *
     * @param valueNamespace 命名空间
     * @return 延迟构造器
     */
    @Override
    public Function<LazyBuildContext, Map<Object, Object>> getLazyBuilder(Object valueNamespace) {
        return builderRegistry.getLazyBuilder(valueNamespace);
    }

    /**
     * 注册提取类型本身的value提取器
     *
     * @param type        类型 Class
     * @param idExtractor Id提取器
     * @return
     */
    @Override
    public final <E> AbstractModelBuilder valueFromSelf(Class<E> type, Function<E, Object> idExtractor) {
        tryCheckConflict();
        extractorRegistry.valueFromSelf(type, idExtractor);
        return this;
    }

    /**
     * 注册指定Model类型的id提取器
     *
     * @param type          Model类型 Class
     * @param idExtractor
     * @param toIdNamespace
     * @return 提取器注册中心
     */
    @Override
    public final <E> AbstractModelBuilder extractId(Class<E> type, Function<E, Object> idExtractor, Object toIdNamespace) {
        tryCheckConflict();
        extractorRegistry.extractId(type, idExtractor, toIdNamespace);
        return this;
    }

    /**
     * 注册指定Model类型的value提取器
     *
     * @param type             Model类型
     * @param valueExtractor   value提取器
     * @param idExtractor      提取出的value的id提取器
     * @param toValueNamespace 提取出的value要放入的命名空间
     * @return 提取器注册中心
     */
    @Override
    public final <E, V> AbstractModelBuilder extractValue(Class<E> type, Function<E, Object> valueExtractor, Function<V, Object> idExtractor, Object toValueNamespace) {
        tryCheckConflict();
        extractorRegistry.extractValue(type, valueExtractor, idExtractor, toValueNamespace);
        return this;
    }

    /**
     * 判断指定类型是否存在value提取器
     *
     * @param type 指定的类型
     * @return 该类型是否存在value提取器
     */
    @Override
    public boolean existsValueExtractor(Class<?> type) {
        return extractorRegistry.existsValueExtractor(type);
    }

    /**
     * 获取指定类型下的id提取器
     *
     * @param type 指定的类型
     * @return id提取器集合
     */
    @Override
    public Set<Function<Object, KeyPair<Set<Object>>>> getIdExtractors(Class<?> type) {
        return extractorRegistry.getIdExtractors(type);
    }

    /**
     * 获取指定类型下的value提取器
     *
     * @param type 指定的类型
     * @return value类型
     */
    @Override
    public Set<Function<Object, KeyPair<Map<Object, Object>>>> getValueExtractors(Class<?> type) {
        return extractorRegistry.getValueExtractors(type);
    }

    /**
     * 执行数据提取
     *
     * @param obj          待提取的原始数据
     * @param buildContext 构造上下文
     * @param buildingTemp 构造临时数据
     */
    @Override
    public void doExtract(Object obj, BuildContext buildContext, BuildingTemp buildingTemp) {
        extractorRegistry.doExtract(obj, buildContext, buildingTemp);
    }

    /**
     * 设置冲突{@link #tryCheckConflict()}时执行的监听器
     *
     * @param listener
     */
    public AbstractModelBuilder onConflictListener(Runnable listener) {
        this.onConflictListener = listener;
        return this;
    }

    /**
     * 检查是否存在冲突（已经开始构建，还在注册相关数据）
     */
    private void tryCheckConflict() {
        if (alreadyBuilt && onConflictListener != null) {
            onConflictListener.run();
        }
    }
}
