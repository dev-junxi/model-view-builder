package com.msl.model.builder.impl;

import com.msl.model.builder.AbstractModelBuilder;
import com.msl.model.builder.ExtractorRegistry;
import com.msl.model.builder.LazyBuilderRegistry;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.builder.context.BuildingTemp;
import com.msl.model.builder.context.LazyBuildContext;
import com.msl.model.utils.StoreUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wanglq
 * Date 2022/11/9
 * Time 16:58
 */
public class DefaultModelBuilder extends AbstractModelBuilder {
    /**
     * 递归调用深度（一次构建允许最多的递归次数），限止无限循环
     */
    private int deep = 30;

    public DefaultModelBuilder() {
    }

    public DefaultModelBuilder(int deep) {
        if (deep <= 0) {
            throw new IllegalArgumentException("deep must be positive");
        }
        this.deep = deep;
    }

    public DefaultModelBuilder(ExtractorRegistry extractorRegistry, LazyBuilderRegistry builderRegistry) {
        super(extractorRegistry, builderRegistry);
    }

    public DefaultModelBuilder(ExtractorRegistry extractorRegistry, LazyBuilderRegistry builderRegistry, int deep) {
        super(extractorRegistry, builderRegistry);
        if (deep <= 0) {
            throw new IllegalArgumentException("deep must be positive");
        }
        this.deep = deep;
    }


    /**
     * 执行构建
     *
     * @param sources      原始数据
     * @param buildContext 构造上下文
     */
    @Override
    protected void doBuild(Iterable<?> sources, BuildContext buildContext) {
        if (sources == null) {
            return;
        }
        if (buildContext instanceof LazyBuildContext) {
            ((LazyBuildContext) buildContext).setupLazyBuilderHolder(this);
        }
        Set<?> toBuilding = toBuilding(sources);
        int cur = deep;
        while (!toBuilding.isEmpty() && --cur >= 0) {
            BuildingTemp temp = new BuildingTemp();
            for (Object object : toBuilding) {
                doExtract(object, buildContext, temp);
            }
            temp.mergeIdToContext(buildContext);
            valueBuild(temp, buildContext);
            temp.mergeValueToContext(buildContext);
            toBuilding = temp.toBuildingValues();
        }
        if (!toBuilding.isEmpty()) {
            throw new RuntimeException("递归调用栈太深，超过限制");
        }
    }

    private void valueBuild(BuildingTemp temp, BuildContext buildContext) {
        temp.getIdsMap().forEach((idNamespace, ids) -> getBuilders(idNamespace).forEach(pair -> {
            Object valueNamespace = pair.getKey();
            Map<Object, Object> values = pair.getValue()
                    .apply(buildContext, StoreUtil.filterIdSet(valueNamespace, ids, buildContext, temp.getValuesMap()));
            temp.mergeValues(valueNamespace, values);
        }));
    }

    private Set<?> toBuilding(Iterable<?> sources) {
        HashSet<Object> set = new HashSet<>();
        for (Object next : sources) {
            if (next instanceof Collection) {
                set.addAll(toBuilding((Collection<?>) next));
            } else if (next instanceof Map) {
                set.addAll(toBuilding(((Map<?, ?>) next).values()));
            } else {
                set.add(next);
            }
        }
        return set;
    }
}
