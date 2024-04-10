package com.msl.model.builder;

import com.google.common.collect.ImmutableList;
import com.msl.model.builder.context.impl.SimpleBuildContext;
import com.msl.model.builder.impl.DefaultModelBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wanglq
 * Date 2022/11/10
 * Time 11:43
 */
public class TestLazyBuild {

    private ModelBuilder builder = new DefaultModelBuilder();

    private LazyDao lazyDao = new LazyDao();

    @Test
    public void test() {
        builder.valueFromSelf(Book.class, Book::getId)
                .valueFromSelf(User.class, User::getId)
                .extractId(Book.class, Book::getUserId, User.class)
                .extractId(User.class, User::getLikeBookId, Book.class)
                .buildValue(User.class, lazyDao::mapUserByIds, User.class)
                .buildValue(Book.class, lazyDao::mapBookByIds, Book.class);
        SimpleBuildContext context = new SimpleBuildContext();
        try {
            builder.buildMulti(lazyDao.listBookByIds(ImmutableList.of(1, 2, 3)), context);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "递归调用栈太深，超过限制");
            e.printStackTrace();
        }

    }

    @Test
    public void test1() {
        builder.valueFromSelf(Book.class, Book::getId)
                .valueFromSelf(User.class, User::getId)
                .extractId(Book.class, Book::getUserId, User.class)
                .extractId(User.class, User::getLikeBookId, Book.class)
                .lazyBuild(User.class, lazyDao::mapUserByIds, User.class)
                .lazyBuild(Book.class, lazyDao::mapBookByIds, Book.class);
        SimpleBuildContext context = new SimpleBuildContext();
        builder.buildMulti(lazyDao.listBookByIds(ImmutableList.of(1, 2, 3)), context);
        System.out.println(context.getIds(Book.class));
        User user = context.getData(User.class, 3);
        System.out.println(user);
        Book likeBook = context.getData(Book.class, user.getLikeBookId());
        Assertions.assertNotNull(likeBook);
    }

    class LazyDao {
        public List<Book> listBookByIds(Collection<Integer> ids) {
            System.out.println("查询book列表：" + ids);
            return ids.stream().map(id -> new Book(id, "book" + id, id + 2)).collect(Collectors.toList());
        }

        public List<User> listUserByIds(Collection<Integer> ids) {
            System.out.println("查询user列表：" + ids);
            return ids.stream().map(id -> new User(id, "user" + id, id + 2)).collect(Collectors.toList());
        }

        public Map<Integer, Book> mapBookByIds(Collection<Integer> ids) {
            return listBookByIds(ids).stream().collect(Collectors.toMap(Book::getId, Function.identity()));
        }

        public Map<Integer, User> mapUserByIds(Collection<Integer> ids) {
            return listUserByIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }
    }

    @Data
    @AllArgsConstructor
    class Book {
        private Integer id;
        private String name;
        private Integer userId;
    }

    @Data
    @AllArgsConstructor
    class User {
        private Integer id;
        private String name;
        private Integer likeBookId;
    }

}
