package com.msl.model.builder;

import com.msl.model.builder.impl.DefaultModelBuilder;
import com.msl.model.builder.model.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author w.vela
 */
class ModelBuilderConflictTest {

    private ModelBuilder builder;

    @Test
    void testBuild1() {
        TestBuildContext buildContext = new TestBuildContext(1);
        boolean[] conflict = {false};
        builder = new DefaultModelBuilder()
                .onConflictListener(() -> conflict[0] = true)
                .lazyBuild(User.class, (TestBuildContext context, Collection<Integer> ids) -> Collections.emptyMap(),
                        "isFans");
        List<Object> sources = new ArrayList<>();
        builder.buildSingle(null, buildContext);
        builder.lazyBuild(User.class, (TestBuildContext context, Collection<Integer> ids) -> Collections.emptyMap(),
                "isFans2");
        builder.lazyBuild(User.class,
                (TestBuildContext context, Collection<Integer> ids) -> Collections.emptyMap(),
                "isFans");
        assertTrue(conflict[0]);
    }

    @Test
    void testBuild2() {
        TestBuildContext buildContext = new TestBuildContext(1);
        boolean[] conflict = {false};
        builder = new DefaultModelBuilder()
                .onConflictListener(() -> conflict[0] = true)
                .extractId(Object.class, it -> it, String.class);
        List<Object> sources = new ArrayList<>();
        builder.buildSingle(null, buildContext);
        builder.extractId(Object.class, it -> it, String.class);
        assertTrue(conflict[0]);
    }

    @Test
    void testBuild3() {
        TestBuildContext buildContext = new TestBuildContext(1);
        boolean[] conflict = {false};
        builder = new DefaultModelBuilder()
                .onConflictListener(() -> conflict[0] = true)
                .extractValue(Object.class, it -> it, it -> it, String.class);
        List<Object> sources = new ArrayList<>();
        builder.buildSingle(null, buildContext);
        builder.extractValue(Object.class, it -> it, it -> it, String.class);
        assertTrue(conflict[0]);
    }
}
