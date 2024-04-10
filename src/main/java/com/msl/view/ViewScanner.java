package com.msl.view;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.msl.model.builder.ModelBuilder;
import com.msl.model.builder.context.BuildContext;
import com.msl.model.utils.ClassScanner;
import com.msl.view.annotation.ViewIgnore;
import com.msl.view.mapper.ViewMapper;
import com.msl.view.mapper.impl.DefaultViewMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author wanglq
 * Date 2022/11/7
 * Time 18:33
 */
public class ViewScanner {

    private Logger log = LoggerFactory.getLogger(ViewScanner.class);
    private ModelBuilder modelBuilder;

    public ViewScanner(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public ViewMapper viewMapper(String[] basePackage) {
        final ViewMapper viewMapper = scan(basePackage);
        return new ViewMapper() {

            private ViewMapper delegate = viewMapper;
            private ModelBuilder builder = modelBuilder;

            @Override
            public <M, V> V map(M model, BuildContext buildContext) {
                builder.buildSingle(model, buildContext);
                return delegate.map(model, buildContext);
            }

            @Override
            public <M, V> List<V> map(Collection<M> models, BuildContext buildContext) {
                builder.buildMulti(models, buildContext);
                return delegate.map(models, buildContext);
            }

            @Override
            public <M, V> ViewMapper addMapper(Class<M> modelType, BiFunction<BuildContext, M, V> viewFactory) {
                delegate.addMapper(modelType, viewFactory);
                return this;
            }

        };
    }

    private final ViewMapper scan(String[] pkg) {
        DefaultViewMapperImpl viewMapper = new DefaultViewMapperImpl();
        try {
            Set<Class<?>> classes = Sets.newHashSet();
            for (String p : pkg) {
                ClassScanner scanner = new ClassScanner();
                scanner.scanning(p, true);
                classes.addAll(scanner.getClasses().values());
            }
            log.info("扫描到View类个数为：{}", classes.size());
            classes.forEach(c -> add(viewMapper, c));
        } catch (Exception e) {
            log.error("Ops.", e);
        }
        return viewMapper;
    }

    private void add(DefaultViewMapperImpl viewMapper, Class<?> type) {
        if (!View.class.isAssignableFrom(type)) {
            return;
        }
        ViewIgnore ignore = type.getAnnotation(ViewIgnore.class);
        if (ignore != null) {
            return;
        }

        Constructor<?>[] constructors = type.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == 2 && params[1] == BuildContext.class) {
                log.info(">>>>>>>>>>>>>>>>>>>> register view [{}] for model [{}], with buildContext.",
                        type.getSimpleName(), params[0].getSimpleName());
                viewMapper.addMapper(params[0], (context, model) -> {
                    try {
                        return constructor.newInstance(model, context);
                    } catch (Exception e) {
                        log.error("fail to construct model:{}", model, e);
                        return null;
                    }
                });
            }
            if (params.length == 1) {
                log.info(">>>>>>>>>>>>>>>>>>>> register view [{}] for model [{}]", type.getSimpleName(),
                        params[0].getSimpleName());
                viewMapper.addMapper(params[0], (buildContext, model) -> {
                    try {
                        return constructor.newInstance(model);
                    } catch (Exception e) {
                        log.error("fail to construct model:{}", model, e);
                        return null;
                    }
                });
            }
        }
    }
}
