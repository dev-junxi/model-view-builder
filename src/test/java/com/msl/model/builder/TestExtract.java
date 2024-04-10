package com.msl.model.builder;

import com.google.common.collect.Maps;
import com.msl.model.builder.context.impl.SimpleBuildContext;
import com.msl.model.builder.impl.DefaultModelBuilder;
import com.msl.model.builder.model.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * @author wanglq
 * Date 2022/11/10
 * Time 10:40
 */
public class TestExtract {
    private Logger logger = LoggerFactory.getLogger(ModelBuilderTest.class);

    private TestDao testDao = new TestDao();

    private ModelBuilder builder = new DefaultModelBuilder();

    @BeforeEach
    void setUp() {
        builder.valueFromSelf(Post.class, Post::getId);
    }

    @Test
    public void testExtract() {
        Collection<Post> posts = testDao.getPosts(Arrays.asList(1L, 2L, 3L)).values();
        SimpleBuildContext context = new SimpleBuildContext();
        builder.buildMulti(posts, context);
        System.out.println(context.getIds(Post.class));
        Assertions.assertEquals(3, context.getIds(Post.class).size());
    }


    class LikeDao {
        public Map<Integer, Integer> getLikeUsers(Collection<Integer> postIds) {
            Map<Integer, Integer> map = Maps.newHashMap();
            if (postIds != null && !postIds.isEmpty()) {
                for (Integer postId : postIds) {
                    map.put(postId, new Random().nextInt(100));
                }
            }
            return map;
        }

    }
}
