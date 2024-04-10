package com.msl.model.builder;

import com.google.common.collect.ImmutableList;
import com.msl.model.builder.context.impl.SimpleBuildContext;
import com.msl.model.builder.impl.DefaultModelBuilder;
import com.msl.model.builder.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wanglq
 * Date 2022/11/8
 * Time 11:29
 */
class ModelBuilderTest {
    private Logger logger = LoggerFactory.getLogger(ModelBuilderTest.class);
    private TestDao testDAO;

    private ModelBuilder builder;

    @BeforeEach
    void setup() {
        testDAO = new TestDao();
        builder = new DefaultModelBuilder()
                .valueFromSelf(User.class, User::getId)
                .valueFromSelf(Post.class, Post::getId)
                .valueFromSelf(Comment.class, Comment::getId)
                .extractId(Comment.class, Comment::getAtUserIds, User.class)
                .extractId(HasUser.class, HasUser::getUserId, User.class)
                .extractValue(Post.class, Post::comments, Comment::getId, Comment.class)
                .buildValue(User.class, testDAO::getUsers)
                .buildValue(Post.class, testDAO::getPosts)
                .buildValue(Comment.class, testDAO::getComments)
                .buildValue(User.class, (TestBuildContext context, Collection<Integer> ids) -> testDAO.isFollowing(context.getVisitorId(), ids), "isFollowing")
                .lazyBuild(User.class, (TestBuildContext context, Collection<Integer> ids) -> testDAO.isFans(context.getVisitorId(), ids), "isFans")
                .lazyBuild(User.class, (TestBuildContext context, Collection<Integer> ids) -> {
                    Map<Integer, Boolean> fans = testDAO.isFans(context.getVisitorId(), ids);
                    logger.debug("build fans for:{}->{},result:{}", context.getVisitorId(), ids, fans);
                    return fans;
                }, "isFans3")
                .lazyBuild(Fake.class, (TestBuildContext context, Collection<Integer> ids) -> testDAO.isFans(context.getVisitorId(), ids), "unreachedLazy")
                .lazyBuild(Fake.class, (TestBuildContext context, Collection<Integer> ids) -> {
                    context.getData("unreachedLazy");
                    return testDAO.isFans(context.getVisitorId(), ids);
                }, "unreachedLazy2");
        System.out.println("builder===>");
        System.out.println(builder);
    }

    @Test
    void testBuild() {
        TestBuildContext buildContext = new TestBuildContext(1);
        List<Object> sources = new ArrayList<>();
        Collection<Post> posts = testDAO.getPosts(Arrays.asList(1L, 2L, 3L)).values();
        posts.forEach(post -> post.setComments(
                testDAO.getComments(post.getCommentIds()).values().stream().collect(toList())));
        sources.addAll(posts);
        sources.addAll(testDAO.getComments(singletonList(3L)).values());
        sources.add(new SubUser(98));
        logger.info("sources===>");
        sources.forEach(o -> logger.info("{}", o));
        testDAO.assertOn();
        builder.buildMulti(sources, buildContext);
        logger.info("buildContext===>");
        logger.info("{}", buildContext);

        assertTrue(testDAO.retrievedFansUserIds.isEmpty());

        Map<Integer, Boolean> isFans = buildContext.getData("isFans");
        logger.info("isFans:{}", isFans);
        isFans.forEach((userId, value) -> assertEquals(
                testDAO.fansMap.get(buildContext.getVisitorId()).contains(userId), value));
        assertFalse(testDAO.retrievedFansUserIds.isEmpty());
        logger.info("retry fans");
        buildContext.getData("isFans");
        logger.info("doing merge");
        buildContext.merge(new SimpleBuildContext());
        testDAO.retrievedFansUserIds.clear();
        logger.info("isFans:{}", buildContext.getData("isFans"));

        // try assert
        for (Object obj : sources) {
            if (obj instanceof Post) {
                Post post = (Post) obj;
                assertEquals(buildContext.getData(Post.class).get(post.getId()), obj);
                assertEquals(post.getUserId(),
                        buildContext.getData(User.class).get(post.getUserId()).getId());
                for (Comment cmt : post.comments()) {
                    assertCmt(buildContext, cmt);
                }
            }
            if (obj instanceof Comment) {
                Comment cmt = (Comment) obj;
                assertCmt(buildContext, cmt);
            }
            if (obj instanceof User) {
                User user = (User) obj;
                assertUser(buildContext, user);
            }
        }

        Map<Long, Boolean> unreachedLazy = buildContext.getData("unreachedLazy2");
        assertTrue(unreachedLazy.isEmpty());
        assertFalse(unreachedLazy.getOrDefault(1L, false));

        logger.info("checking nodes.");
        buildContext.getData(User.class).values().forEach(user -> assertUser(buildContext, user));
        logger.info("fin.");
    }

    @Test
    void testNullBuild() {
        TestBuildContext buildContext = new TestBuildContext(1);
        builder.buildSingle(null, buildContext);
        buildContext.getData("t").put("a", "c");
        System.out.println("checking...");
        Map<Integer, Boolean> isFans = buildContext.getData("isFans3");
        assertFalse(isFans.getOrDefault(1, false));
        System.out.println("fin.");
    }

    @Test
    void testMerge() {
        TestBuildContext buildContext = new TestBuildContext(1);
        List<User> users = new ArrayList<>(testDAO.getUsers(ImmutableList.of(1, 2, 3)).values());
        builder.buildMulti(users, buildContext);
        Map<Integer, Boolean> isFans = buildContext.getData("isFans3");
        System.out.println("isFans:" + isFans);
        users.forEach(user -> assertNotNull(isFans.get(user.getId())));

        TestBuildContext other = new TestBuildContext(1);
        List<User> users2 = new ArrayList<>(testDAO.getUsers(ImmutableList.of(3, 4, 5)).values());
        builder.buildMulti(users2, other);
        Map<Integer, Boolean> isFans2 = other.getData("isFans3");
        System.out.println("isFans2:" + isFans2);
        users2.forEach(user -> assertNotNull(isFans2.get(user.getId())));

        buildContext.merge(other);
        System.out.println("after merged.");
        System.out.println("users:" + buildContext.getData(User.class));

        Map<Integer, Boolean> merged = buildContext.getData("isFans3");
        System.out.println("merged:" + merged);
        for (int i = 1; i <= 5; i++) {
            assertNotNull(merged.get(i));
        }
        System.out.println("fin.");
    }

    @Test
    void testDuplicateMerge() {
        TestBuildContext mainBuildContext = new TestBuildContext(1);

        TestBuildContext buildContext = new TestBuildContext(1);
        builder.buildMulti(emptyMap().values(), buildContext);
        mainBuildContext.merge(buildContext);

        TestBuildContext buildContext2 = new TestBuildContext(1);
        Map<Integer, User> byIdsFailFast = testDAO.getUsers(ImmutableList.of(1, 2));
        builder.buildMulti(byIdsFailFast.values(), buildContext2);
        Map<Integer, Boolean> isFans3 = buildContext2.getData("isFans3");
        System.out.println("[test] " + isFans3);
        assertFalse(isFans3.isEmpty());

        mainBuildContext.merge(buildContext2);

        isFans3 = mainBuildContext.getData("isFans3");
        System.out.println("[test] " + isFans3);
        assertFalse(isFans3.isEmpty());
    }

    private void assertUser(TestBuildContext buildContext, User user) {
        assertNotNull(buildContext.getData("isFollowing").get(user.getId()));
    }

    private void assertCmt(TestBuildContext buildContext, Comment cmt) {
        assertEquals(buildContext.getData(Comment.class).get(cmt.getId()), cmt);
        assertEquals(cmt.getUserId(),
                buildContext.getData(User.class).get(cmt.getUserId()).getId());
        if (cmt.getAtUserIds() != null) {
            for (Integer atUserId : cmt.getAtUserIds()) {
                assertEquals(atUserId, buildContext.getData(User.class).get(atUserId).getId());
            }
        }
    }

}