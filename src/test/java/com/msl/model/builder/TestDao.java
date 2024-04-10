package com.msl.model.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.msl.model.builder.model.Comment;
import com.msl.model.builder.model.Post;
import com.msl.model.builder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author wanglq
 * Date 2022/11/10
 * Time 10:46
 */
public class TestDao {
    Logger logger = LoggerFactory.getLogger(TestDao.class);

    static final int USER_MAX = 100;
    final Map<Long, Post> posts = ImmutableList
            .of(new Post(1, 1, null),
                    new Post(2, 1, Arrays.asList(1L, 2L, 3L)),
                    new Post(3, 2, Arrays.asList(4L, 5L)))
            .stream().collect(toMap(Post::getId, identity()));

    final Map<Long, Comment> cmts = ImmutableList
            .of(new Comment(1, 1, null), new Comment(2, 2, null), new Comment(3, 1, null),
                    new Comment(4, 2, Arrays.asList(2, 3)),
                    new Comment(5, 11, Arrays.asList(2, 99)))
            .stream().collect(toMap(Comment::getId, identity()));

    final Multimap<Integer, Integer> followingMap = HashMultimap.create();
    final Multimap<Integer, Integer> fansMap = HashMultimap.create();
    Set<Integer> retreievedUserIds;
    Set<Long> retreievedPostIds;
    Set<Long> retreievedCommentIds;
    Set<Integer> retrievedFollowUserIds;
    Set<Integer> retrievedFansUserIds;

    {
        followingMap.put(1, 5);
        followingMap.put(1, 2);
    }

    {
        fansMap.put(1, 5);
        fansMap.put(1, 99);
    }

    Map<Integer, User> getUsers(Collection<Integer> ids) {
        if (retreievedUserIds != null) {
            logger.info("try to get users:{}", ids);
            for (Integer id : ids) {
                assertTrue(retreievedUserIds.add(id));
            }
        }
        return ids.stream().filter(i -> i <= USER_MAX).collect(toMap(identity(), User::new));
    }

    Map<Long, Post> getPosts(Collection<Long> ids) {
        if (retreievedPostIds != null) {
            logger.info("try to get posts:{}", ids);
            for (Long id : ids) {
                assertTrue(retreievedPostIds.add(id));
            }
        }
        return Maps.filterKeys(posts, ids::contains);
    }

    Map<Long, Comment> getComments(Collection<Long> ids) {
        if (ids == null) {
            return emptyMap();
        }
        if (retreievedCommentIds != null) {
            logger.info("try to get cmts:{}", ids);
            for (Long id : ids) {
                assertTrue(retreievedCommentIds.add(id));
            }
        }
        return Maps.filterKeys(cmts, ids::contains);
    }

    Map<Integer, Boolean> isFollowing(int fromUserId, Collection<Integer> ids) {
        if (retrievedFollowUserIds != null) {
            logger.info("try to get followings:{}->{}", fromUserId, ids);
            for (Integer id : ids) {
                assertTrue(retrievedFollowUserIds.add(id));
            }
        }
        Collection<Integer> followings = followingMap.get(fromUserId);
        return ids.stream().collect(toMap(identity(), followings::contains));
    }

    Map<Integer, Boolean> isFans(int fromUserId, Collection<Integer> ids) {
        if (retrievedFansUserIds != null) {
            logger.info("try to get fans:{}->{}", fromUserId, ids);
            for (Integer id : ids) {
                assertTrue(retrievedFansUserIds.add(id));
            }
        }
        Collection<Integer> fans = fansMap.get(fromUserId);
        return ids.stream().collect(toMap(identity(), fans::contains));
    }

    void assertOn() {
        logger.info("assert on.");
        retreievedUserIds = new HashSet<>();
        retreievedPostIds = new HashSet<>();
        retreievedCommentIds = new HashSet<>();
        retrievedFollowUserIds = new HashSet<>();
        retrievedFansUserIds = new HashSet<>();
    }

}
