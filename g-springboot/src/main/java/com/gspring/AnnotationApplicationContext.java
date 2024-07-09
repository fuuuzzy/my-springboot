package com.gspring;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.gspring.gannotation.*;
import com.gspring.gannotation.springmvc.Controller;
import com.gspring.gioc.BeanDefinition;
import com.gspring.gioc.ScopeEnum;
import com.gspring.gioc.aware.AspectBeanPostProcessor;
import com.gspring.gioc.aware.BeanNameAware;
import com.gspring.gioc.aware.BeanPostProcessor;
import com.gspring.gioc.aware.InitializingBean;
import com.gspring.mybaties.MapperProxyFactory;
import com.gspring.mybaties.annotation.Mapper;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiangGang
 * @date 2023-01-08 13:41
 * @Description 容器
 */
public class AnnotationApplicationContext {
    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(AnnotationApplicationContext.class);
    /**
     * 配置类
     */
    private Class<?> configClass;

    /**
     * 类的后缀
     */
    private static final String CLASS_RESOURCE_END = ".class";
    /**
     * 单例池 存放单例bean
     */
    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    /**
     * bean定义信息Map
     */
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * bean后置处理器
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    /**
     * 切面类集合
     */
    private final List<Class<?>> aspectList = new ArrayList<>();

    /**
     * 切点集合
     */
    private final Set<String> pointList = new HashSet<>();
    /**
     * 切面类的后置处理器
     */
    private final AspectBeanPostProcessor aspectBeanPostProcessor = new AspectBeanPostProcessor();

    /**
     * 读取的类的集合
     */
    private final List<String> readClassNameList = new ArrayList<>();

    /**
     * 启动容器
     *
     * @param configClass 配置类
     */
    public AnnotationApplicationContext(Class<?> configClass) {
        register(configClass);
        refresh();
    }

    /**
     * 空参构造
     */
    public AnnotationApplicationContext() {
    }

    /**
     * 获取单例池
     *
     * @return {@link ConcurrentHashMap}
     */

    public Map<String, Object> getiocContainerMap() {
        return singletonObjects;
    }

    /**
     * 注册配置类
     *
     * @param configClass 配置类
     */
    public void register(Class<?> configClass) {
        this.configClass = configClass;
    }

    /**
     * 刷新配置类
     */
    public void refresh() {
        if (configClass == null) {
            throw new RuntimeException("请先注册配置类");
        }
        //清空容器
        singletonObjects.clear();
        beanDefinitionMap.clear();
        beanPostProcessorList.clear();
        aspectList.clear();
        pointList.clear();
        //有springBoot注解，默认扫描当前包下的所有类
        handleSpringBootAnnotation();
        isAnnotationComponentScan(configClass);
        aspectBeanPostProcessor.setAspectList(aspectList);
        aspectBeanPostProcessor.setPointList(pointList);
        //注入Bean
        if (!beanDefinitionMap.isEmpty()) {
            inversionOfControl();
        }
    }

    /**
     * 处理springBoot注解
     */
    private void handleSpringBootAnnotation() {
        if (configClass.isAnnotationPresent(MySpringBootApplication.class)) {
            MySpringBootApplication springBootApplicationAnnotation = configClass.getAnnotation(MySpringBootApplication.class);
            //获取当前启动类的包路径
            URL resource = configClass.getResource("");
            if (resource != null) {
                scanPackage(resource.getFile());
            }
            //自动装配
            autoConfigurationBean(springBootApplicationAnnotation);
        }
    }

    /**
     * 自动装配
     *
     * @param springBootApplicationAnnotation springBoot注解
     */
    private void autoConfigurationBean(MySpringBootApplication springBootApplicationAnnotation) {
        Import importAnnotation = springBootApplicationAnnotation.annotationType().getAnnotation(Import.class);
        Class<?>[] executeImportClass = importAnnotation.value();
        try {
            for (Class<?> clazz : executeImportClass) {
                boolean flag = true;
                Class<?>[] autoConfigurationClasses = new Class[0];
                for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                    Class<?> aClass = entry.getValue().getClazz();
                    if (clazz.isAssignableFrom(aClass)) {
                        autoConfigurationClasses = (Class<?>[]) aClass.getMethods()[0].invoke(aClass.getDeclaredConstructor().newInstance());
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    Class<?> aClass = configClass.getClassLoader().loadClass("com.gspring.boot.autoconfiguration.impl.DefaultSelectImportClass");
                    Method method = aClass.getMethod("selectImportClass");
                    autoConfigurationClasses = (Class<?>[]) method.invoke(aClass.getConstructor().newInstance());
                }
                for (Class<?> needAutoconfigurationClass : autoConfigurationClasses) {
                    loadClass(configClass.getClassLoader(), needAutoconfigurationClass.getName());
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据包路径扫描包下的所有类
     *
     * @param url 包路径
     */
    private void scanPackage(String url) {
        List<String> classNameList = new ArrayList<>();
        //递归查询包下的所有类
        File file = new File(url);
        File[] files = file.listFiles();
        if (files != null) {
            traverseFolders(files, classNameList);
        }

        for (String className : classNameList) {
            loadClass(AnnotationApplicationContext.class.getClassLoader(), className);
        }
    }

    /**
     * 加载类
     *
     * @param classLoader 类加载器
     * @param className   类名
     */
    private void loadClass(ClassLoader classLoader, String className) {
        try {
            //判断是否重复加载类
            if (readClassNameList.contains(className)) {
                return;
            } else {
                readClassNameList.add(className);
            }
            //通过类加载器加载类对象
            Class<?> concurrentLoadClazz = classLoader.loadClass(className);
            //注解不加载
            if (concurrentLoadClazz.isAnnotation()) {
                return;
            }
            if (concurrentLoadClazz.isAnnotationPresent(Import.class)) {
                Class<?>[] classes = concurrentLoadClazz.getAnnotation(Import.class).value();
                for (Class<?> aClass : classes) {
                    loadClass(classLoader, aClass.getName());
                }
            }
            //判断是否为组件
            String beanName = "";
            boolean flag = true;
            if (concurrentLoadClazz.isAnnotationPresent(Component.class) ||
                    concurrentLoadClazz.isAnnotationPresent(Controller.class) ||
                    concurrentLoadClazz.isAnnotationPresent(Service.class)) {
                beanName = handleComponentAnnotation(concurrentLoadClazz);
            } else if (concurrentLoadClazz.isAnnotationPresent(Configuration.class)) {
                beanName = handleConfigurationAnnotation(concurrentLoadClazz);
            } else if (concurrentLoadClazz.isAnnotationPresent(MySpringBootApplication.class)) {
                beanName = "applicationConfig";
            } else if (concurrentLoadClazz.isAnnotationPresent(Mapper.class)) {
                beanName = handleMapperAnnotation(concurrentLoadClazz);
            } else {
                flag = false;
            }
            createPostProcessorAndDefinition(concurrentLoadClazz, beanName, flag);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String handleMapperAnnotation(Class<?> concurrentLoadClazz) throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Mapper mapperAnnotation = concurrentLoadClazz.getAnnotation(Mapper.class);
        return getBeanName(concurrentLoadClazz, mapperAnnotation);
    }

    /**
     * 递归查询包下的所有类
     *
     * @param files         文件集
     * @param classNameList 类名集合
     */
    private void traverseFolders(File[] files, List<String> classNameList) {
        for (File file : files) {
            if (file.isDirectory()) {
                //文件夹处理
                File[] f1 = file.listFiles();
                if (f1 != null) {
                    traverseFolders(f1, classNameList);
                }
            } else {
                //处理单个文件
                handleFileForList(classNameList, file);
            }
        }
    }

    /**
     * 处理文件放入List中
     *
     * @param classNameList 类集合
     * @param file          文件
     */
    private static void handleFileForList(List<String> classNameList, File file) {
        //获取文件名真实路径
        String[] split = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("classes"),
                file.getAbsolutePath().indexOf(CLASS_RESOURCE_END)).split("/");
        StringBuilder className = new StringBuilder();
        for (String str : split) {
            if ("classes".equals(str)) {
                continue;
            }
            className.append(str).append(".");
        }
        className.deleteCharAt(className.length() - 1);
        if (file.getAbsolutePath().endsWith(CLASS_RESOURCE_END)) {
            classNameList.add(className.toString());
        }
    }

    /**
     * IOC容器
     */
    private void inversionOfControl() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            if (ScopeEnum.SINGLETON.getValue().equals(beanDefinition.getScope())) {
                Object bean = createBean(entry.getKey(), beanDefinition);
                singletonObjects.put(entry.getKey(), bean);
            }
        }
        //依赖注入
        dependencyInjection();
    }

    /**
     * 依赖注入
     */
    private void dependencyInjection() {
        for (Object bean : singletonObjects.values()) {
            Class<?> beanClazz = bean.getClass();
            Field[] beanFields = beanClazz.getDeclaredFields();
            for (Field beanField : beanFields) {
                if (beanField.isAnnotationPresent(Autowired.class)) {
                    beanField.setAccessible(true);
                    beanDefinitionMap.forEach((k, v) -> {
                        if (v.getClazz().getInterfaces().length > 0) {
                            propertiesInjection(v.getClazz() == beanField.getType() || Arrays.asList(v.getClazz().getInterfaces()).contains(beanField.getType()), k, v, beanField, bean);
                        } else {
                            propertiesInjection(v.getClazz() == beanField.getType(), k, v, beanField, bean);
                        }
                    });
                }
            }
        }
    }

    /**
     * 属性注入
     *
     * @param isInjection 是否注入
     * @param beanName    bean名称
     * @param v           bean定义
     * @param beanField   bean属性
     * @param bean        bean实例
     */
    private void propertiesInjection(boolean isInjection, String beanName, BeanDefinition v, Field beanField, Object bean) {
        if (isInjection) {
            try {
                if (ScopeEnum.SINGLETON.getValue().equals(v.getScope())) {
                    beanField.set(bean, singletonObjects.get(beanName));
                } else {
                    beanField.set(bean, createBean(beanName, v));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据Bean定义创建Bean
     *
     * @param beanDefinition bean定义
     * @return {@link Object} Bean
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        Object instant;
        //创建Bean
        try {
            if (clazz.isInterface()) {
                instant = MapperProxyFactory.getMapper(clazz);
            } else {
                instant = clazz.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        //处理BeanNameAware
        if (instant instanceof BeanNameAware) {
            ((BeanNameAware) instant).setBeanName(beanName);
        }
        //处理BeanPostProcessor 前置处理
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            instant = beanPostProcessor.postProcessBeforeInitialization(instant, beanName);
        }
        //初始化Bean
        if (instant instanceof InitializingBean) {
            ((InitializingBean) instant).afterPropertiesSet();
        }
        //处理BeanPostProcessor 后置处理
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            instant = beanPostProcessor.postProcessAfterInitialization(instant, beanName);
        }
        //处理切面类
        instant = aspectBeanPostProcessor.postProcessAfterInitialization(instant, beanName);
        return instant;
    }

    /**
     * 处理配置类注解
     *
     * @param concurrentLoadClazz 类对象
     * @return {@link String} BeanName
     */
    private String handleConfigurationAnnotation(Class<?> concurrentLoadClazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Configuration configurationAnnotation = concurrentLoadClazz.getAnnotation(Configuration.class);
        //有扫描组件注解则去扫描包路径下的类
        isAnnotationComponentScan(concurrentLoadClazz);
        //判断是否有自定义beanName
        String beanName = concurrentLoadClazz.getSimpleName();
        if (!"".equals(configurationAnnotation.value())) {
            beanName = configurationAnnotation.value();
        } else {
            //默认beanName为类名首字母小写
            if (Character.isUpperCase(beanName.charAt(0))) {
                beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
            }
        }
        analysisBeanAnnotation(concurrentLoadClazz);
        return beanName;
    }

    /**
     * 解析Bean注解
     *
     * @param concurrentLoadClazz 类
     * @throws InstantiationException    异常
     * @throws IllegalAccessException    异常
     * @throws InvocationTargetException 异常
     * @throws NoSuchMethodException     异常
     */
    private void analysisBeanAnnotation(Class<?> concurrentLoadClazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        //识别每个方法上面是否有Bean注解 有则将返回类型放入bean定义，待后续生产bean
        Method[] methods = concurrentLoadClazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Bean.class)) {
                Bean beanAnnotation = method.getAnnotation(Bean.class);
                String beansName = "";
                Class<?> returnType = method.getReturnType();

                if (method.getReturnType().isInterface()) {
                    Class<?> invoke = method.invoke(concurrentLoadClazz.getDeclaredConstructor().newInstance()).getClass();
                    beansName = invoke.getSimpleName();
                    returnType = invoke;
                } else {
                    beansName = returnType.getSimpleName();
                }
                ClassLoader classLoader = concurrentLoadClazz.getClassLoader();
                boolean flag = true;
                try {
                    if (returnType.getSimpleName().contains("Tomcat")) {
                        classLoader.loadClass("org.apache.catalina.startup.Tomcat");
                    } else {
                        classLoader.loadClass("org.eclipse.jetty.util.log.JettyLogHandler");
                    }
                } catch (ClassNotFoundException e) {
                    LOG.info("不需要加载Jetty");
                    flag = false;
                }
                //判断是否有自定义beanName
                if (!"".equals(beanAnnotation.value())) {
                    beansName = beanAnnotation.value();
                } else {
                    //默认beanName为类名首字母小写
                    if (Character.isUpperCase(beansName.charAt(0))) {
                        beansName = Character.toLowerCase(beansName.charAt(0)) + beansName.substring(1);
                    }
                }
                createPostProcessorAndDefinition(returnType, beansName, flag);
            }
        }
    }

    /**
     * 判断是否有扫描组件注解
     *
     * @param concurrentLoadClazz 类
     */
    private void isAnnotationComponentScan(Class<?> concurrentLoadClazz) {
        if (concurrentLoadClazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = concurrentLoadClazz.getAnnotation(ComponentScan.class);
            String path = componentScan.value().replace(".", "/");
            URL resource = ClassLoader.getSystemClassLoader().getResource(path);
            if (resource != null) {
                scanPackage(resource.getFile());
            }
        }
    }

    /**
     * 创建后置处理和bean定义
     *
     * @param concurrentLoadClazz 类
     * @param beanName            类名
     * @param flag                是否需要被创建
     * @throws InstantiationException    异常
     * @throws IllegalAccessException    异常
     * @throws InvocationTargetException 异常
     * @throws NoSuchMethodException     异常
     */
    private void createPostProcessorAndDefinition(Class<?> concurrentLoadClazz, String beanName, boolean flag) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (!flag || "".equals(beanName)) {
            return;
        }
        if (configClass.isAnnotationPresent(EnableAspectAutoProxy.class) && concurrentLoadClazz.isAnnotationPresent(Aspect.class)) {
            handleAspect(concurrentLoadClazz);
        }
        //判断该类是否为BeanPostProcessor后置处理器
        if (BeanPostProcessor.class.isAssignableFrom(concurrentLoadClazz)) {
            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) concurrentLoadClazz.getDeclaredConstructor().newInstance();
            //加入后置处理器集合
            beanPostProcessorList.add(beanPostProcessor);
        }
        //构建Bean定义集合
        createBeanDefinition(concurrentLoadClazz, beanName);
    }

    /**
     * 处理Component注解
     *
     * @param concurrentLoadClazz 类
     * @return @{@link String} 类名
     */
    private String handleComponentAnnotation(Class<?> concurrentLoadClazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Component componentAnnotation = concurrentLoadClazz.getAnnotation(Component.class);
        Service serviceAnnotation = concurrentLoadClazz.getAnnotation(Service.class);
        Controller controllerAnnotation = concurrentLoadClazz.getAnnotation(Controller.class);
        if (componentAnnotation != null) {
            return getBeanName(concurrentLoadClazz, componentAnnotation);
        }
        if (serviceAnnotation != null) {
            return getBeanName(concurrentLoadClazz, serviceAnnotation);
        }
        if (controllerAnnotation != null) {
            return getBeanName(concurrentLoadClazz, controllerAnnotation);
        }
        return "";
    }

    /**
     * 获取beanName
     *
     * @param concurrentLoadClazz 类
     * @param componentAnnotation 注解
     * @return @{@link String} beanName
     */
    private String getBeanName(Class<?> concurrentLoadClazz, Annotation componentAnnotation) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        //判断是否有自定义beanName
        Class<? extends Annotation> annotationClass = componentAnnotation.getClass();
        String beanName = concurrentLoadClazz.getSimpleName();
        if (componentAnnotation.annotationType() == Controller.class) {
            if (!"".equals(annotationClass.getMethod("name").invoke(componentAnnotation))) {
                beanName = (String) annotationClass.getMethod("name").invoke(componentAnnotation);
            } else {
                //默认beanName为类名首字母小写
                if (Character.isUpperCase(beanName.charAt(0))) {
                    beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                }
            }
        } else {
            if (!"".equals(annotationClass.getMethod("value").invoke(componentAnnotation))) {
                beanName = (String) annotationClass.getMethod("value").invoke(componentAnnotation);
            } else {
                //默认beanName为类名首字母小写
                if (Character.isUpperCase(beanName.charAt(0))) {
                    beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                }
            }
        }
        analysisBeanAnnotation(concurrentLoadClazz);
        return beanName;
    }

    /**
     * 处理是否为切面类
     *
     * @param concurrentLoadClazz 类
     */
    private void handleAspect(Class<?> concurrentLoadClazz) {
        //切面类
        Method[] methods = concurrentLoadClazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Point.class)) {
                Point annotation = method.getAnnotation(Point.class);
                String pointValue = annotation.value();
                if ("@annotation(Zgh)".equals(pointValue)) {
                    pointValue = pointValue.substring(pointValue.indexOf("("), pointValue.indexOf(")"));
                    pointList.add(pointValue);
                }
            }
        }
        aspectList.add(concurrentLoadClazz);
    }

    /**
     * 构建Bean定义集合
     *
     * @param concurrentLoadClazz 当前类对象
     * @param beanName            类名
     */
    private void createBeanDefinition(Class<?> concurrentLoadClazz, String beanName) {
        //构建Bean定义
        BeanDefinition beanDefinition = new BeanDefinition();
        //类对象放进去
        beanDefinition.setClazz(concurrentLoadClazz);
        //判断是否为单例
        if (concurrentLoadClazz.isAnnotationPresent(Scope.class)) {
            Scope scopeAnnotation = concurrentLoadClazz.getAnnotation(Scope.class);
            beanDefinition.setScope(scopeAnnotation.value());
        } else {
            beanDefinition.setScope(ScopeEnum.SINGLETON.getValue());
        }
        //判断beanName是否重复
        if (beanDefinitionMap.containsKey(beanName)) {
            throw new RuntimeException("beanName repeat!");
        }
        //加入Bean定义集合
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * 根据beanName获取bean
     *
     * @param beanName 类名称
     * @return {@link Object} bean对象
     */
    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (ScopeEnum.SINGLETON.getValue().equals(beanDefinition.getScope())) {
                return singletonObjects.getOrDefault(beanName, null);
            } else {
                //多例
                return createBean(beanName, beanDefinition);
            }
        } else {
            throw new RuntimeException("beanName不存在");
        }
    }

    /**
     * 根据类型获取Bean
     *
     * @param clazz 类型
     * @param <V>   泛型
     * @return {@link Map} bean对象
     */
    public <V> Map<String, V> getBeanByType(Class<V> clazz) {
        Map<String, V> map = new HashMap<>(beanDefinitionMap.size());
        if (clazz.isInterface()) {
            return getBeanByInterface(clazz, map);
        }
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (entry.getValue().getClazz().isAssignableFrom(clazz)) {
                map.put(entry.getKey(), (V) getBean(entry.getKey()));
            }
        }
        return map;
    }

    /**
     * 根据接口获取bean
     *
     * @param clazz 类
     * @param map   集合
     * @param <V>   泛型
     * @return {@link Map} bean对象
     */
    public <V> Map<String, V> getBeanByInterface(Class<V> clazz, Map<String, V> map) {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?>[] interfaces = entry.getValue().getClazz().getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (anInterface.isAssignableFrom(clazz)) {
                    map.put(entry.getKey(), (V) getBean(entry.getKey()));
                }
            }
        }
        return map;
    }
}
