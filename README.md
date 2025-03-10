# ModelViewBuilder参考文档

# ModelViewBuilde参考文档-

## 1 概述

在后台查询数据的过程中，经常会依赖查询别的数据，此组件目的就是将这种依赖查询自动化、隐藏化。通过配置对象的依赖关系，自动完成对象的依赖构建，然后通过上下文进行view映射。并且这种依赖构建支持懒加载，即只有当你返回给前端真正需要某个依赖数据时，才会执行数据查询过程。

## 2 快速开始

### 2.1 引包

```plaintext
<dependency>
    <groupId>com.msl</groupId>
    <artifactId>model-view-builder</artifactId>
</dependency>
```
> 最新版本，可查询 [msl-parent](https://t.clxkj.cn/msl-java/msl-parent/blob/master/pom.xml) 项目pom.xml，或者[公司私服](http://139.129.222.24:8081/#browse/browse/components\:maven-releases)

### 2.2 通过 ModelBuilder 指定构建依赖

例如：有一个书籍类 Book，里面有字段 authorId 代表对应的作者的用户（User）id

```plaintext
@Data
public class Book {
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 作者id
     */
    private Integer authorId;
}
```
```plaintext
@Data
public class User {
    private Integer id;
    /**
     * 名称
     */
    private String name;
}
```

现在通过 ModelBuilder 指定其中的构建依赖 1、提取 Book 中的 authorId 到对应的id命名空间（可以理解为，执行构建时会把当前上下文中所有 Book 对象中的 authorId 集中到一个容器中）

```plaintext
ModelBuilder modelBuilder = new DefaultModelBuilder()
        //将 Book.getAuthorId() 返回的数据放到 id 命名空间 User.class
        .extractId(Book.class, Book::getAuthorId, User.class);
```

2、指定如何通过 id 集合查询用户

```plaintext
// 在modelbuilder 中指定根据 id 命名空间 User.class 中的用户id 通过 Test 类中的 listUserByIds 方法查询对应的用户数据，将用户数据放入 value 命名空间 User.class（默认不用指定）
modelBuilder.buildValue(User.class, Test::listUserByIds);


/**
 * 通过id集合查询用户信息
 *
 * @param ids 用户id集合
 * @return
 */
public static Map<Integer, User> listUserByIds(Collection<Integer> ids) {
    //执行相应查询操作……
    return Optional.ofEmpty(ids)
            .streamMap(Collection::stream)
            .sMap(id -> new User(id, "用户" + id))
            .toMap(User::getId);
}
```

### 2.3 通过上下文构建实体，可以通过上下文获取对应依赖数据

```plaintext
//查询所有书籍
List<Book> books = listAllBook();
//创建构建上下文
BuildContext context = new SimpleBuildContext();
//执行构建
modelBuilder.buildMulti(books, context);
//从上下文中获取用户信息
for (Book book : books) {
    User user = context.getData(User.class, book.getAuthorId());
    System.out.println(user);
}
```

### 2.4 使用 ViewMapper 映射 view 对象

在上述 2.3 的代码展示里其实已经完成了依赖查询，但是这种查询需要封装到一个对象当中，下次有相同的依赖查询时候，不需要重复这段代码。将包含有依赖数据的对象返回到上一层后，上一层可以根据自己的需要灵活组装字段进行返回 定义相关 View 对象

```plaintext
public class UserView extends View<User> {
    public UserView(User source, BuildContext context) {
        super(source, context);
    }
}

public class BookView extends View<Book> {
    public BookView(Book source, BuildContext context) {
        super(source, context);
    }

    /**
     * 获取作者数据
     *
     * @return
     */
    public UserView getAuthor() {
        return build(User.class, source.getAuthorId(), UserView::new);
    }
}
```
> 如上在 BookView 类除了包含的 Book 数据还含有作者对应的用户数据（UserView）。这样上一层在使的时候，如果需要展示作者名称的时候，直接可以从 UserView 中获取。

## 3 进阶使用

### 3.1 核心解析

**1. 命名空间** 分为 id 命名空间和 value 命名空间，用于标识这是哪一类 id 和 value 数据。像上面的 User.class 既是 id 名称空间标识，所有用户id数据，也是 value 命名空间，标识所有用户实体数据。可以从构建上下文获取指定命名空间的 id 数据和 value 数据。

**2. 提取器注册中心 ExtractorRegistry** 用来注册、存储所有 id 提取器和 value 提取器，它们的作用就是执行构建时，将数据为按注册好的提取器执行提取，得到不同类别的新数据放到相应的空间当中

```plaintext
//注册 id 提取器，将 Book 中 getAuthorId() 方法获取的数据放入  User.class 命名空间中
modelBuilder.extractId(Book.class, Book::getAuthorId, User.class);
//注册 value 提取器，将 Book 对象 放入 Book.class 命名空间中，并以 Book.getId() 返回的数据作为对象标识
//以下两种方法等同
modelBuilder.valueFromSelf(Book.class, Book::getId);
modelBuilder.extractValue(Book.class, b -> b, Book::getId, Book.class);
```

**3. 核心构建器 ModelBuilder** 这个接口就是控制整个构建过程的，核心方法就是

```plaintext
/**
 * 构造列表数据
 *
 * @param sources      原始数据
 * @param buildContext 构造上下文
 */
void buildMulti(Iterable<?> sources, BuildContext buildContext);
```

具体实现类请查看源码

**4. 构建上下文 BuildContext** 构建过程中的上下文环境，缓存所有构造数据，通过它可以拿到想要的数据

```plaintext
/**
 * 根据命名空间获取空间集合数据
 * 各空间数据以Map形式返回
 *
 * @param namespace 命名空间
 * @param <K>       空间集合数据key类型
 * @param <V>       空间集合数据value类型
 * @return 指定的空间集合数据
 */
<K, V> Map<K, V> getData(Object namespace);

/**
 * 根据命名空间获取空间内指定id的数据
 * 各空间数据以Map形式返回
 *
 * @param namespace 命名空间
 * @param key       数据id
 * @param <K>       空间集合数据key类型
 * @param <V>       空间集合数据value类型
 * @return 指定数据
 */
<K, V> V getData(Object namespace, K key);
```

### 3.2 自定义 BuilderContext

可以将一些初始化参数放入 Context 中参于后面的构建，例如：登陆用户所属公司，后面的构建都可以基于该公司完成

```plaintext
public static void main(String[] args) {
    DefaultModelBuilder builder = new DefaultModelBuilder();
    //注册查询
    builder.<Integer, CompanyBuilderContext>buildValue(User.class,
            (context, userIds) -> CompanyTest.listBookByUser(userIds, context.getCompanyNo()), "booksOfUser");
    /*使用构建*/
    //查询到的用户
    List<User> list = new ArrayList<>();
    //获取当前登陆用户公司
    Long companyNo = 123L;
    CompanyBuilderContext context = new CompanyBuilderContext(companyNo);
    builder.buildMulti(list, context);
    for (User user : list) {
        //获取用户所著书籍
        List<Book> books = context.getData("booksOfUser", user.getId());
        System.out.println(books);
    }

}

/**
 * 批量查询用户所著书籍（当前操作公司下）
 *
 * @param userIds   用户id集合
 * @param companyNo 公司编码
 * @return
 */
public static Map<Integer, List<Book>> listBookByUser(Collection<Integer> userIds, Long companyNo) {
    return new HashMap<>();
}
```

### 3.3 懒加载

系统支持懒加载数据，提供相应的接口 LazyBuilderRegistry、LazyBuildContext

```plaintext
//正常加载
modelBuilder.buildValue(User.class, Test::listUserByIds);
//懒加载
modelBuilder.lazyBuild(User.class, Test::listUserByIds);
```
> 这个方法在上面有提到，就是注册根据 User.class 下的用户 id 数据查询用户数据，注意两者的区别：正常加载每次构建，都会立马判断上下文中是否存在用户 id 数据，有的话就会执行一遍查询，不管查到的用户数据会不会被使用；懒加载的话，则是每次尝试从上下文中获取用户数据时，才会执行一遍查询。例如：之前的获取书籍列表的方法，正常加载的话，每次查到书籍时，就会相应的把书籍对应的作者查询一遍；而懒加载只有调用 getUerView 方法才会去执行查询

### 3.4 基于反射获取 View 声明

由于的 view 的声明大同小异，可以按照统一的规则定义，然后通过反射添加到 viewMapper 当中。系统提供有 ViewScanner 用于扫描需要声明的 view。

```plaintext
public static void main(String[] args) {
    DefaultModelBuilder modelBuilder = new DefaultModelBuilder();

    //定义扫描器
    ClassScanner scanner = new ClassScanner();
    DefaultViewMapperImpl viewMapper = new DefaultViewMapperImpl();
    try {
        Set<Class<?>> classes = Sets.newHashSet();
        //扫描包 com.msl 及其子包下的 view
        scanner.scanning("com.msl", true);
        classes.forEach(c -> add(viewMapper, c));
    } catch (Exception e) {
        e.printStackTrace();
    }
    //这里 viewMapper 里就包含有所有的 model 和 view 之间的映射关系了
}

/**
 * 将 view 类添加到 viewMapper 中
 *
 * @param viewMapper
 * @param type
 */
private static void add(DefaultViewMapperImpl viewMapper, Class<?> type) {
    //具体逻辑自行实现
}
```

## 4 更新日志

*   [x] 2.0.0 初始版本，实现基本框架
    
*   [x] 2.1.0 @OneMatch支持匹配多个类
    

## 5 扩展组件 dao-spring-boot-starter

此组件是在 model-view-builder 组件的基础上，结合 mybaits-plus 封装好常用查询功能，接入后只需继承定义好的基类，就能获取关联查询的能力。

### 5.1引包

```plaintext
<dependency>
    <groupId>com.msl</groupId>
    <artifactId>dao-spring-boot-starter</artifactId>
</dependency>
```

### 5.2定义相关类

1.  **实体继承 HasKey** ：Book implements HasKey<Integer>，实现 gainKey方法获取对象惟一标识，通过注解 @KeyColumn 指定对应的数据库字段```/\*\*
    
2.  获取惟一标识\*
    
3.  @return id\*/@Override@KeyColumn("id")public Integer gainKey() {return id;}```
    
4.  **定义 mapper** 继承 mybatis-plus 提供的 BaseMapper ： interface BookMapper extends BaseMapper<Book>
    
5.  **定义 dao** 继承 BaseDao ： interface BookDao extends BaseDao<BookMapper, Book, Integer>
    
6.  **定义 daoImpl** 继承 BaseDaoImpl : class BookDaoImpl extends BaseDaoImpl<BookMapper, Book, Integer> implements BookDao
    
7.  **定义 view** 继承 View ： class BookView extends View<Book>
    
8.  **定义 service** 继承 BaseService ： interface BookService extends BaseService<BookView, Book, Integer>
    
9.  **定义 serviceImpl** 继承 BaseServiceImpl ： class BookServiceImpl extends BaseServiceImpl<BookDao, BookView, Book, Integer>implements BookService
    

## 6 更新日志

*   [x] 1.0.0 初始版本
    
*   [x] 1.0.2 HasId 更换为 HasKey ，支持任意类型的惟一标识
    
*   [x] @OneMatch匹配多个类后的相应支持