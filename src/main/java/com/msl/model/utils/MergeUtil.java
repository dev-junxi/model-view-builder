package com.msl.model.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 10:33
 */
public class MergeUtil {
    public static Map<Object, Object> merge(Map<Object, Object> map1, Map<Object, Object> map2) {
        map1.putAll(map2);
        return map1;
    }

    public static <C extends Collection<Object>> C merge(C c1, C c2) {
        c1.addAll(c2);
        return c1;
    }
}
