package com.msl.model.utils;

public interface HasKey<K> {
    /**
     * 获取惟一标识
     *
     * @return id
     */
    K gainKey();
}
