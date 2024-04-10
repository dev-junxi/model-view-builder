package com.msl.base;

import java.util.Map;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 11:51
 */
public class KeyPair<V> implements Map.Entry<Object, V> {
    private final Object key;
    private final V value;

    public KeyPair(Object key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

}
