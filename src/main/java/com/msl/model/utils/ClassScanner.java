package com.msl.model.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @Author
 * @Description 包扫描器
 * @CopyRight
 */
public class ClassScanner {
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    /**
     * 类文件过滤器,只扫描一级类
     */
    private FilenameFilter javaClassFilter;
    /**
     * Java字节码文件后缀
     */
    private final String CLASS_FILE_SUFFIX = ".class";
    /**
     * 包路径根路劲
     */
    private String bashPath;

    public ClassScanner() {
        javaClassFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // 排除内部类
                return isNotInnerClass(name);
            }
        };
        bashPath = getRealFilePath(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")));
    }

    /**
     * 扫描指定包, Jar或本地
     *
     * @param packagePath 包路径
     * @param recursive   是否扫描子包
     * @return Integer 类数量
     */
    public Integer scanning(String packagePath, boolean recursive) {
        Enumeration<URL> dir;
        String filePackPath = packagePath.replace('.', '/');
        try {
            // 得到指定路径中所有的资源文件
            dir = Thread.currentThread().getContextClassLoader().getResources(filePackPath);

            // 遍历资源文件
            while (dir.hasMoreElements()) {
                URL url = dir.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    File file = new File(getRealFilePath(url));
                    scan0(file, packagePath, recursive);
                } else if ("jar".equals(protocol)) {
                    scanJ(url, packagePath, recursive);
                }
            }
        } catch (
                Exception e) {
            throw new RuntimeException(e);
        }

        return classes.size();
    }

    private String getRealFilePath(URL url) {
        String path = url.getPath();
        if (System.getProperty("file.separator").equals("\\")) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 扫描Jar包下所有class
     *
     * @param url         jar-url路径
     * @param packagePath 包路径
     * @param recursive   是否递归遍历子包
     */
    private void scanJ(URL url, String packagePath, boolean recursive) throws IOException, ClassNotFoundException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        JarFile jarFile = connection.getJarFile();

        // 遍历Jar包
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String fileName = jarEntry.getName();

            if (jarEntry.isDirectory() || !isNotInnerClass(fileName)) {
                continue;
            }

            // .class
            if (fileName.endsWith(CLASS_FILE_SUFFIX)) {
                String className = fileName.substring(0, fileName.indexOf('.')).replace('/', '.');
                String packageName = className.substring(0, className.lastIndexOf('.'));
                if (!packageName.startsWith(packagePath) ||
                        (packageName.length() > packagePath.length() && !recursive)) {
                    continue;
                }
                classes.put(className, Class.forName(className));
            }

        }
    }

    /**
     * 扫描文件夹下所有class
     *
     * @param dir         Java包对应文件系统文件夹
     * @param packagePath 包路径
     * @throws ClassNotFoundException 类未找到异常
     */
    private void scan0(File dir, String packagePath, boolean recursive) throws ClassNotFoundException {
        File[] fs = dir.listFiles(javaClassFilter);
        for (int i = 0; fs != null && i < fs.length; i++) {
            File f = fs[i];
            String fileName = f.getName();
            if (f.isDirectory() && recursive) {
                scan0(f, packagePath + "." + fileName, recursive);
            } else {
                // 跳过其他文件
                int idx = fileName.lastIndexOf(CLASS_FILE_SUFFIX);
                if (idx != -1) {
                    String className = packagePath + "." + fileName.substring(0, idx);
                    classes.put(className, Class.forName(className));
                }
            }


        }
    }

    private static String getPackageByPath(File classFile, String exclude) {
        if (classFile == null || classFile.isDirectory()) {
            return null;
        }

        String path = classFile.getAbsolutePath().replace("\\", "/");

        path = path.substring(path.indexOf(exclude) + exclude.length()).replace('/', '.');
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        if (path.endsWith(".")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.substring(0, path.lastIndexOf('.'));
    }

    /**
     * @return Map&lt;String,Class&lt;?&gt;&gt; K:类全名, V:Class字节码
     * @Title: getClasses
     * @Description 获取包中所有类
     */
    public Map<String, Class<?>> getClasses() {
        return classes;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ClassScanner cs2 = new ClassScanner();
        int c2 = cs2.scanning("com.msl.model.builder", true);
        System.out.println(c2);
        System.out.println(cs2.getClasses().keySet());


        ClassScanner scanner = new ClassScanner();
        scanner.scanning("com.google.common.util", true);
        System.out.println(scanner.getClasses().keySet());
    }

    private boolean isNotInnerClass(String name) {
        return !name.contains("$");
    }
}