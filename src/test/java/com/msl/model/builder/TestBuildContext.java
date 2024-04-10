package com.msl.model.builder;

import com.msl.model.builder.context.impl.SimpleBuildContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wanglq
 * Date 2022/11/8
 * Time 13:21
 */
public class TestBuildContext extends SimpleBuildContext {
    private final int visitorId;

    public TestBuildContext(int visitorId) {
        super(new ConcurrentHashMap<>(1, 0.75f, 2));
        this.visitorId = visitorId;
    }

    public int getVisitorId() {
        return visitorId;
    }

    @Override
    public String toString() {
        return "TestBuildContext{" +
                "visitorId=" + visitorId +
                '}';
    }
}
