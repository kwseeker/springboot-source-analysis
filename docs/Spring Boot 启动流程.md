# Spring boot 启动流程

分析 Spring Boot 启动流程是为了遇到问题快速定位源码；
1）Spring配置中使用"-"连接组成的字段是怎么解析到驼峰式的变量中的？
2）@Configuration 与 @Component 处理的差异？
3）...

调用栈（缩进表示调用深度）
```


//实例化所有异常报告类 SpringBootExceptionReporter（FailureAnalyzers）
    loadFailureAnalyzers:70, FailureAnalyzers (org.springframework.boot.diagnostics)
    <init>:65, FailureAnalyzers (org.springframework.boot.diagnostics)
    <init>:59, FailureAnalyzers (org.springframework.boot.diagnostics)
    newInstance0:-1, NativeConstructorAccessorImpl (sun.reflect)
    newInstance:62, NativeConstructorAccessorImpl (sun.reflect)
    newInstance:45, DelegatingConstructorAccessorImpl (sun.reflect)
    newInstance:423, Constructor (java.lang.reflect)
    instantiateClass:172, BeanUtils (org.springframework.beans)
    createSpringFactoriesInstances:446, SpringApplication (org.springframework.boot)
    getSpringFactoriesInstances:429, SpringApplication (org.springframework.boot)
run:311, SpringApplication (org.springframework.boot)
//根据应用类型选择要创建的应用上下文的类型，并加载类然后使用反射机制实例化（很重要的点）
    <init>:74, AnnotationConfigServletWebServerApplicationContext (org.springframework.boot.web.servlet.context)
    newInstance0:-1, NativeConstructorAccessorImpl (sun.reflect)
    newInstance:62, NativeConstructorAccessorImpl (sun.reflect)
    newInstance:45, DelegatingConstructorAccessorImpl (sun.reflect)
    newInstance:423, Constructor (java.lang.reflect)
    instantiateClass:172, BeanUtils (org.springframework.beans)
    instantiateClass:122, BeanUtils (org.springframework.beans)
    createApplicationContext:607, SpringApplication (org.springframework.boot)
run:310, SpringApplication (org.springframework.boot)
    //环境准备（外部化配置的第2阶段）
        //FileEncodingApplicationListener
        
        //DelegatingApplicationListener
        
        //BackgroundPreinitializer
        
        //ClasspathLoggingApplicationListener
        
        //LoggingApplicationListener
        initializeWithConventions:85, AbstractLoggingSystem (org.springframework.boot.logging)
        initialize:60, AbstractLoggingSystem (org.springframework.boot.logging)
        initialize:117, LogbackLoggingSystem (org.springframework.boot.logging.logback)
        initializeSystem:292, LoggingApplicationListener (org.springframework.boot.context.logging)
        initialize:265, LoggingApplicationListener (org.springframework.boot.context.logging)
        onApplicationEnvironmentPreparedEvent:228, LoggingApplicationListener (org.springframework.boot.context.logging)
        onApplicationEvent:201, LoggingApplicationListener (org.springframework.boot.context.logging)    
        //AnsiOutputApplicationListener
        
        //ConfigFileApplicationListener
          
    //依次通知监听 ApplicationEnvironmentPrepareEvent 事件的监听器
    doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
    invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
    multicastEvent:139, SimpleApplicationEventMulticaster (org.springframework.context.event)
    multicastEvent:127, SimpleApplicationEventMulticaster (org.springframework.context.event)
    environmentPrepared:75, EventPublishingRunListener (org.springframework.boot.context.event)
    environmentPrepared:54, SpringApplicationRunListeners (org.springframework.boot)
    prepareEnvironment:347, SpringApplication (org.springframework.boot)
    //设置属性转换服务，以及添加默认属性源(addLast)和命令行属性源(addFirst)（key：springApplicationCommandLineArgs）
    configure:91, ApplicationConversionService (org.springframework.boot.convert)
    <init>:52, ApplicationConversionService (org.springframework.boot.convert)
    <init>:45, ApplicationConversionService (org.springframework.boot.convert)
    getSharedInstance:71, ApplicationConversionService (org.springframework.boot.convert)
    configureEnvironment:486, SpringApplication (org.springframework.boot)
    prepareEnvironment:346, SpringApplication (org.springframework.boot)
    //完成定制属性源（4种， addLast）
    customizePropertySources:78, StandardEnvironment (org.springframework.core.env)
    customizePropertySources:90, StandardServletEnvironment (org.springframework.web.context.support)
    <init>:124, AbstractEnvironment (org.springframework.core.env)
    <init>:54, StandardEnvironment (org.springframework.core.env)
    <init>:45, StandardServletEnvironment (org.springframework.web.context.support)
    getOrCreateEnvironment:463, SpringApplication (org.springframework.boot)
    prepareEnvironment:345, SpringApplication (org.springframework.boot)
run:306, SpringApplication (org.springframework.boot)
//装配ApplicationArguments(读取命令行参数)
run:305, SpringApplication (org.springframework.boot)
        //LiquibaseServiceLocatorApplicationListener 设置 Liquibase ServiceLocator
        onApplicationEvent:43, LiquibaseServiceLocatorApplicationListener (org.springframework.boot.liquibase)
        onApplicationEvent:35, LiquibaseServiceLocatorApplicationListener (org.springframework.boot.liquibase)
        doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
        invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
        //（条件不满足）DelegatingApplicationListener将Spring事件委派给配置 context.listener.classes所指定的多个 ApplicationListener 
        //BackgroundPreinitializer 背景环境预初始化（开一个新线程基于CountDownLatch计数器执行6个初始化操作）
        onApplicationEvent:75, BackgroundPreinitializer (org.springframework.boot.autoconfigure)
        onApplicationEvent:52, BackgroundPreinitializer (org.springframework.boot.autoconfigure)
        doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
        invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
        //LoggingApplicationListener 日志系统预初始化
        onApplicationStartingEvent:218, LoggingApplicationListener (org.springframework.boot.context.logging)
        onApplicationEvent:198, LoggingApplicationListener (org.springframework.boot.context.logging)
        doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
        invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
    multicastEvent:139, SimpleApplicationEventMulticaster (org.springframework.context.event)
    multicastEvent:127, SimpleApplicationEventMulticaster (org.springframework.context.event)
    starting:69, EventPublishingRunListener（org.springframework.boot.context.event）
    starting:48, SpringApplicationRunListeners (org.springframework.boot)
    run:302, SpringApplication (org.springframework.boot)
        //创建SpringApplication实例
        <init>:249, SpringApplication (org.springframework.boot)
run:1260, SpringApplication (org.springframework.boot)
run:1248, SpringApplication (org.springframework.boot)
//调用SpringApplication静态方法run()传入启动类class和启动参数
main:10, SimpleAppApplication (top.kwseeker.simpleapp)
```

## 1. SpringApplication 初始化(资源准备)

### 1.1 SpringApplication 构造

+ 创建SpringApplication实例

    - primarySource（什么用？）
    
    - 推断Web应用类型 webApplicationType（什么用？）
    
        使用ClassLoader检查导入的包的Class是否存在来判断应用类型是WebApplicationType.REACTIVE、WebApplicationType.SERVLET、WebApplicationType.NONE
        的哪一种。
        
    - 加载Spring应用上下文初始化器 initializers（什么用？）
        
        搜索并实例化加载的jar中 META-INF/spring.factories 资源中指定的 org.springframework.context.ApplicationContextInitializer
        的实现类(6个)
        ```
        0 = "org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer"
        1 = "org.springframework.boot.context.ContextIdApplicationContextInitializer"
        2 = "org.springframework.boot.context.config.DelegatingApplicationContextInitializer"
        3 = "org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer"
        4 = "org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer"
        5 = "org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener"
        ```
        将初始化器Set集合存入SpringApplication实例的 initializers 。
        
        ```
        Resource: 
        jar:file:/Users/lee/.m2/repository/org/springframework/boot/spring-boot/2.1.6.RELEASE/spring-boot-2.1.6.RELEASE.jar!/META-INF/spring.factories
        jar:file:/Users/lee/.m2/repository/org/springframework/spring-beans/5.1.8.RELEASE/spring-beans-5.1.8.RELEASE.jar!/META-INF/spring.factories
        jar:file:/Users/lee/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.1.6.RELEASE/spring-boot-autoconfigure-2.1.6.RELEASE.jar!/META-INF/spring.factories
        ...
        ```
        
    - 加载Spring应用事件监听器 listeners（什么用？）
    
        搜索并实例化加载的jar中 META-INF/spring.factories 资源中指定的 org.springframework.context.ApplicationListener
        的实现类(10个)
        
        ```
        0 = "org.springframework.boot.ClearCachesApplicationListener"
        1 = "org.springframework.boot.builder.ParentContextCloserApplicationListener"
        2 = "org.springframework.boot.context.FileEncodingApplicationListener"
        3 = "org.springframework.boot.context.config.AnsiOutputApplicationListener"
        4 = "org.springframework.boot.context.config.ConfigFileApplicationListener"
        5 = "org.springframework.boot.context.config.DelegatingApplicationListener"
        6 = "org.springframework.boot.context.logging.ClasspathLoggingApplicationListener"
        7 = "org.springframework.boot.context.logging.LoggingApplicationListener"
        8 = "org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener"
        9 = "org.springframework.boot.autoconfigure.BackgroundPreinitializer"
        ```
        将初始化器Set集合存入SpringApplication实例的 listeners 。
    
    - 推导应用引导类 mainApplicationClass
    
        从堆栈信息中查找main()方法，推导应用引导类，存入 mainApplicationClass。
        当前堆栈信息如下，所以为 "top.kwseeker.simpleapp.SimpleAppApplication"。
        ```
        deduceMainApplicationClass:278, SpringApplication (org.springframework.boot)
        <init>:271, SpringApplication (org.springframework.boot)
        <init>:249, SpringApplication (org.springframework.boot)
        run:1260, SpringApplication (org.springframework.boot)
        run:1248, SpringApplication (org.springframework.boot)
        main:10, SimpleAppApplication (top.kwseeker.simpleapp)
        ```

### 1.2 SpringApplication 配置(可选) 

+ 自定义 SpringApplication（参考官方文档 [23.3 Customizing SpringApplication](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/htmlsingle/#boot-features-customizing-spring-application)）
    
    - 调整SpringApplication设置
    
        调整 SpringApplication 中包括 1.1 中的设置项的众多设置。
        
        ```
        //main()当前写法
        SpringApplication.run(SimpleAppApplication.class, args); //根据源码分析知相当于 new SpringApplication(new Class[] {SimpleAppApplication.class}).run(args);
        //如果要修改或者添加配置可以这么写
        SpringApplication app = new SpringApplication(new Class[] {SimpleAppApplication.class});
        //要修改或添加的设置
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("prod");
        app.run(args);
        ```

    - 增加SpringApplication配置源
    
        SpringApplication配置源：主配置类（测试应用的 SimpleAppApplication）, @Configuration注解Class,
        XML配置文件、package。
        
        配置源来源 primarySources environment的propertySources defaultProperties addCommandLineProperties。
    
    - 调整Spring Boot 外部化配置
    
        即在 application.properties 配置，可以覆盖SpringApplication.setDefaultProperties方法的设置。
        比如 
        ```
        spring.config.location
        spring.config.additional-location
        ```
    
## 2. SpringApplication 运行（执行run()方法）

### 2.1 SpringApplication 准备（进一步准备所需资源）

+ 实例化并启动StopWatch

    验证性能，和启动逻辑没什么关系。
    
+ 设置java.awt.headless系统属性为true - 没有图形化界面

+ 获取所有 SpringApplicationRunListener 实现类并实例化、排序（顺序由 @Order @Priority 指定）

    这里需要停一下，仔细研究下 Spring 事件&监听机制， 参考《Spring事件&监听机制.md》。
    
    SpringApplicationRunListener 的生命周期回调方法依次发布 Spring Boot 的内置事件（前面1.1实例化的10个事件监听器用于监听这些事件，注意是被动通知）。
    查看其源码就是一个基于事件链驱动的配置流程。
        
    Spring Boot 内建事件以及对应监听器参考：《Spring Boot 编程思想》P506。
    
    下面跟一下这个基于事件链驱动的配置流程：  
    
    1）入口是 SpringApplicationRunListeners#starting()方法，内部循环调用每个SpringApplicationRunListener（默认只有一个）的starting()方法。
    发布第一个事件 ApplicationStartingEvent, 监听这个事件的监听器有 LoggingApplicationListener、BackgroundPreinitializer 和 LiquibaseServiceLocatorApplicationListener。
    ```
    public void starting() {
        this.initialMulticaster.multicastEvent(
            new ApplicationStartingEvent(this.application, this.args));
    }
    ```
    
    1.1）执行日志初始化
    ```
    onApplicationStartingEvent:218, LoggingApplicationListener (org.springframework.boot.context.logging)
    onApplicationEvent:198, LoggingApplicationListener (org.springframework.boot.context.logging)
    doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
    invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
    multicastEvent:139, SimpleApplicationEventMulticaster (org.springframework.context.event)
    ```
    执行LoggerApplicationListener#onApplicationEvent()方法
    ```
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            onApplicationStartingEvent((ApplicationStartingEvent) event);
        }
        else 
            ...
    }
    ```
    日志系统预初始化, 然后我们知道了日志系统预初始化的地方，关于内部实现原理暂时不深入，后面有需要再看，先创建个Markdown（Spring Boot 日志系统初始化.md），后面补充。
    ```
    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        this.loggingSystem = LoggingSystem
                .get(event.getSpringApplication().getClassLoader());
        this.loggingSystem.beforeInitialize();
    }
    ```
    
    1.2）背景环境预初始化（开一个新线程基于CountDownLatch计数器执行6个初始化操作）
    ```
    private void performPreinitialization() {
        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    runSafely(new ConversionServiceInitializer());
                    runSafely(new ValidationInitializer());
                    runSafely(new MessageConverterInitializer());
                    runSafely(new MBeanFactoryInitializer());
                    runSafely(new JacksonInitializer());
                    runSafely(new CharsetInitializer());
                    preinitializationComplete.countDown();
                }

                public void runSafely(Runnable runnable) {
                    try {
                        runnable.run();
                    }
                    catch (Throwable ex) {
                        // Ignore
                    }
                }

            }, "background-preinit");
            thread.start();
        }
        catch (Exception ex) {
            // This will fail on GAE where creating threads is prohibited. We can safely
            // continue but startup will be slightly slower as the initialization will now
            // happen on the main thread.
            preinitializationComplete.countDown();
        }
    }
    ```
    
    1.3）查看是否有手动配置 Liquibase ServiceLocator, 有的话取代默认 ServiceLocator。
    Liquibase 是一个用于数据库重构和迁移（比如将测试数据库的数据库结构同步到生产库上）的开源工具。
    
    [Spring Boot 集成Liquibase: 85.5.2 Execute Liquibase Database Migrations on Startup](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/htmlsingle/#howto-execute-liquibase-database-migrations-on-startup)
    [Liquibase document](http://www.liquibase.org/documentation/index.html)
    [Github 上 Liquibase 简单应用 Spring Boot Demo](https://github.com/xieshuang/t-blog)
    
+ 装配 ApplicationArguments
        
    底层实现基于 SimpleCommandLinePropertySource。
    
+ 准备 ConfigurationEnvironment(什么用？)

    导入外部化配置的第二步。
    [导入外部化配置的流程](https://pic2.zhimg.com/v2-c4e3b17a33718a38f85b391bbbfb51ad_r.jpg)
    
    1）定制属性源（这4个属性源都好生疏？）
    ```
    0 = {PropertySource$StubPropertySource@2315} "StubPropertySource {name='servletConfigInitParams'}"
    1 = {PropertySource$StubPropertySource@2462} "StubPropertySource {name='servletContextInitParams'}"
    2 = {MapPropertySource@2463} "MapPropertySource {name='systemProperties'}"
    3 = {SystemEnvironmentPropertySource@2464} "SystemEnvironmentPropertySource {name='systemEnvironment'}"
    ```
    
    2) 设置属性转换服务，以及添加默认属性源(addLast)和命令行属性源(addFirst)（key：springApplicationCommandLineArgs）
      
    - ConfigureEnvironment (I)
        
        * AbstractEnvironment (AC)
            
            存储配置属性的仓库。
    
        * StandardServletEnvironment (C)
        
            核心类,包含 propertySources(配置源) 和 propertyResolver（配置属性转换器）
    
    - PropertyResolver (I)
       
        看接口方法知接口功能是通过key获取对应配置属性value的。
       
        * AbstractPropertyResolver (AC)
        
            属性占位符 "${}", 属性分隔符 ":"。 
            
        * PropertySourcesPropertyResolver (C)

        * ConversionService (I)
              
            属性值读取后的格式化和转换器。
            
    3) 通过 SpringApplicationRunListener 触发 ApplicationEnvironmentPreparedEvent。
        
        3.1) 日志系统初始化（前面曾经监听ApplicationStartingEvent进行过预初始化）
        
            主要是根据加载的配置，进行日志系统真正的初始化。
            
        3.2) ...
        
    4) 将获取到的 environment 中的 "spring.main" 配置绑定到 SpringApplication 的 source 中。
    
    5) ConfigurationPropertySources.attach(environment) ?

+ 根据"spring.beaninfo.ignore"设置是否跳过搜索BeanInfo类，默认为true

+ 打印 Spring Boot Banner 到终端（第一条日志）

+ 创建 Spring 应用上下文 ConfigurationApplicationContext

    1) 根据应用类型选择要创建的应用上下文的类型，并加载类然后使用反射机制实例化（很重要的点） 
    
        "org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext";

        ![](../images/Spring%20Boot%20应用上下文.png)

    2) 两个重要成员
    
        ```
        public AnnotationConfigServletWebServerApplicationContext() {
            this.reader = new AnnotatedBeanDefinitionReader(this);
            this.scanner = new ClassPathBeanDefinitionScanner(this);
        }
        ```
        
+ 创建 SpringBootExceptionReporter（FailureAnalyzers）对象集合,用于报告 Spring Boot 运行异常
    
    从加载的 META-INF/spring.factories 路径中读取  SpringBootExceptionReporter.class
    类型class的名字（org.springframework.boot.diagnostics.FailureAnalyzers）；
    然后使用configurableApplicationContext（BeanFactory功能）实例化所有 interface org.springframework.boot.diagnostics.FailureAnalyzer
    实现类。
    
+  Spring应用上下文运行前准备(SpringApplication#preparedContext 方法开始到 SpringApplicationRunListeners#contextPrepared 截止) 

    1) 设置应用上下文 ConfigurableEnvironment
    
        ```
        context.setEnvironment(environment);
        ```
    
        应用上下文保持 ConfigurableEnvironment 的引用；
        并设置 AnnotationBeanDefinitionReader 和 ClassPathBeanDefinitionScanner 的来源为此 ConfigurableEnvironment，
        看名字知道是加载bean时用的处理类。
        
    2) Spring 应用上下文后置处理
    
        - 设置 Spring应用上下文 beanFactory 的属性转换服务实例
            ```
            context.getBeanFactory().setConversionService(
                    ApplicationConversionService.getSharedInstance());
            ```
            
    3) 运用 Spring应用上下文初始化器 
    
        ApplicationContextInitializer 用于定制 context 或 environment。
        [76.3 Customize the Environment or ApplicationContext Before It Starts](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/htmlsingle/#howto-customize-the-environment-or-application-context)
        
        依次初始化 Spring应用上下文 的6个初始化器（前面从 META-INF/spring.factories中读取到）。
        ```
        0 = "org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer"
        1 = "org.springframework.boot.context.ContextIdApplicationContextInitializer"
        2 = "org.springframework.boot.context.config.DelegatingApplicationContextInitializer"
        3 = "org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer"
        4 = "org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer"
        5 = "org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener"
        ```
        
        + ConfigurationWarningsApplicationContextInitializer
            
            报告 Spring 容器常见的错误配置。会向 context 上下文对象中添加一个 BeanFactoryPostProcessor，
            然后在 refresh ApplicationContext 的时候会被调用到。
            
        + ContextIdApplicationContextInitializer
        
            给ApplicationContext设置一个ID。
            从 environment 中读取 spring.application.name 属性，若没有则默认为 "application", 
            使用此属性值创建 ContextIdApplicationContextInitializer#ContextId 实例，并注册为 singleton bean(key: "org.springframework.boot.context.ContextIdApplicationContextInitializer$ContextId")。
            但是在 http://localhost:8888/actuator/beans 中并没有找到这个bean（TODO：为什么找不到？actuator/beans 没有显示全部的bean么？）；
            这个bean确实存在于 Spring应用上下文(AnnotationConfigServletWebServerApplicationContext)#beanFactory(DefaultListableBeanFactory)#singletonObjects(ConcurrentHashMap) 中。
            ```
            spring.application.name = demoApp
            ```
        
        + DelegatingApplicationContextInitializer
        
            将初始化的工作委托给context.initializer.classes环境变量指定的初始化器。
        
        + ServerPortInfoApplicationContextInitializer
        
            监听 EmbeddedServletContainerInitializedEvent 类型的事件。
            然后将内嵌的Web服务器使用的端口给设置到 ApplicationContext 中。
        
        + SharedMetadataReaderFactoryContextInitializer
        
            创建一个用于在 ConfigurationClassPostProcessor 和 Spring Boot 间共享的 CachingMetadataReaderFactory。
        
        + ConditionEvaluationReportLoggingListener
        
            将ConditionEvaluationReport写入到log，一般的日志的级别是DEBUG，出问题的话使用INFO级别。通过增加ApplicationListener的方式实现。
          
    4) 发送 ApplicationContextInitializedEvent 事件
    
        + BackgroundPreInitializer（条件不满足）

        + DelegatingApplicationListener（条件不满足）
    
    5) 选用日志配置，默认为"default";
    
+ Spring 应用上下文装载阶段

    6) 注册 singleton bean "springApplicationArguments"、"springBootBanner";
    不允许 BeanDefinition 覆盖（默认，可以手动通过setter修改）。 
    
    7) 合并上下文配置源, 并加载应用上下文配置源 sources(类名、包名或XML配置资源路径)
    
    8) 发送 ApplicationPreparedEvent

        ```
        0 = {ConfigFileApplicationListener@4207} 
        1 = {AnsiOutputApplicationListener@4212} 
        2 = {LoggingApplicationListener@4217} 
        3 = {ClasspathLoggingApplicationListener@4222} 
        4 = {BackgroundPreinitializer@3424} 
        5 = {DelegatingApplicationListener@3425} 
        6 = {ParentContextCloserApplicationListener@4235} 
        7 = {ClearCachesApplicationListener@4239} 
        8 = {FileEncodingApplicationListener@4240} 
        9 = {LiquibaseServiceLocatorApplicationListener@4241} 
        ```

        具体参考每个监听器对此事件的处理逻辑。
    
### 2.2 Spring 应用上下文启动

前面全都是在做准备工作，这里开始进入Spring生命周期，Spring Boot 核心特性将随之启动，
如：组件自动装配、嵌入式容器启动Production-Ready特性。
上下文启动后事件 ContextRefreshedEvent随之传播。

这部分加载的组件很多，只以数据源的自动装配为例进行分析（DataSourceAutoConfiguration.class）。
可以参考Mybatis自动装配源码分析部分。

```
this.refreshContext(context);
```
 
```
@Override
public void refresh() throws BeansException, IllegalStateException {
 synchronized (this.startupShutdownMonitor) {
     // Prepare this context for refreshing.
     // 1）清空元数据缓存 CachingMetadataReaderFactory#clearCache
     // 2) 初始化属性源
     //     0 = {ConfigurationPropertySourcesPropertySource@4443} "ConfigurationPropertySourcesPropertySource {name='configurationProperties'}"
     //     1 = {PropertySource$StubPropertySource@4444} "StubPropertySource {name='servletConfigInitParams'}"      //这个和下面的是在Spring MVC 的XML中经常配置的参数
     //     2 = {PropertySource$StubPropertySource@4445} "StubPropertySource {name='servletContextInitParams'}"
     //     3 = {MapPropertySource@4446} "MapPropertySource {name='systemProperties'}"
     //     4 = {SystemEnvironmentPropertySourceEnvironmentPostProcessor$OriginAwareSystemEnvironmentPropertySource@4447} "OriginAwareSystemEnvironmentPropertySource {name='systemEnvironment'}"
     //     5 = {RandomValuePropertySource@4448} "RandomValuePropertySource {name='random'}"
     //     6 = {OriginTrackedMapPropertySource@4449} "OriginTrackedMapPropertySource {name='applicationConfig: [classpath:/application.properties]'}"
     // 3）检查需要的属性？
     // 4）将之前的事件监听器添加到应用上下文的事件监听器（LinkedHashSet）
     prepareRefresh();

     // Tell the subclass to refresh the internal bean factory.
     ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

     // Prepare the bean factory for use in this context.
     // 准备Spring应用上下文的 bean factory（设置解析bean定义的工具、格式、忽略依赖的bean、处理依赖注入）
     // 添加Bean的 post-processor, 用于定制 Bean 初始化前后的个性化处理。
     // 检测 LoadTimeWeaver Bean, 存在的话添加 LoadTimeWeaverAwareProcessor。LoadTimeWeaver 用于支持切面的代码织入
     // 注册 environment Bean
     prepareBeanFactory(beanFactory);

     try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         // 
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         // 发送 ContextRefreshedEvent
         finishRefresh();
     }

     catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
             logger.warn("Exception encountered during context initialization - " +
                     "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
     }

     finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
     }
 }
}
```

### 2.3 Spring 应用上下文启动后处理



## 3. SpringApplication 结束与退出 

### 3.1 SpringApplication正常结束与退出

### 3.2 SpringApplication异常结束与退出

