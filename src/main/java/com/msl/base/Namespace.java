package com.msl.base;

import java.util.Objects;

/**
 * @author wanglq
 * Date 2022/11/4
 * Time 17:10
 */
public class Namespace {
    private Object identify;

    public Namespace(Object identify) {
        this.identify = Objects.requireNonNull(identify);
    }


    @Override
    public int hashCode() {
        return identify.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Namespace) && identify.equals(((Namespace) obj).identify);
    }
}
