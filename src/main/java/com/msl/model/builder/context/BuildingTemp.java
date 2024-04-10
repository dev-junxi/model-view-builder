package com.msl.model.builder.context;

import com.google.common.collect.Maps;
import com.msl.model.utils.MergeUtil;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * 构造中的临时数据
 *
 * @author wanglq
 * Date 2022/11/9
 * Time 17:46
 */
public class BuildingTemp {
    private Map<Object, Set<Object>> idsMap;
    private Map<Object, Map<Object, Object>> valuesMap;

    public BuildingTemp() {
        this.idsMap = Maps.newHashMap();
        this.valuesMap = Maps.newHashMap();
    }

    public void mergeIds(Object idNamespace, Set<Object> ids) {
        idsMap.merge(idNamespace, ids, MergeUtil::merge);
    }

    public void mergeValues(Object valueNamespace, Map<Object, Object> values) {
        valuesMap.merge(valueNamespace, values, MergeUtil::merge);
    }

    public void mergeIdToContext(final BuildContext buildContext) {
        idsMap.forEach((k, v) -> buildContext.getIds(k).addAll(v));
    }

    public void mergeValueToContext(BuildContext buildContext) {
        valuesMap.forEach((k, v) -> buildContext.getCachedData(k).putAll(v));
    }

    public Map<Object, Set<Object>> getIdsMap() {
        return idsMap;
    }

    public Map<Object, Map<Object, Object>> getValuesMap() {
        return valuesMap;
    }

    public Set<?> toBuildingValues() {
        return valuesMap.values().stream().flatMap(v -> v.values().stream()).collect(toSet());
    }
}
