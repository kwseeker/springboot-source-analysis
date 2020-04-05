# Spring IoC 实现原理

参考资料：
《Spring源码深度解读》第2、3、4、5、6章

Spring IoC的目的：对象的创建、存储及统一管理。

Spring IoC 在 spring-core 中实现。

Spring Core 实现的功能：

IoC Container, Events, Resources, i18n, Validation, Data Binding, Type Conversion, SpEL, AOP

## Spring IoC 体系结构

如果根据前面说的 IoC 需求，自己设计一套 API 应该怎么设计？

对象的创建：首先需要获取所有bean类的定义，来自于XML或者注解，Spring Boot全是来自注解；
那么要扫描所有的XML文件bean标签和被Bean类注解注释的class的信息；
然后加载类并实例化。

对象的存储：将实例化的对象存储到容器类中, 判断是否存在某名字的bean`containsBean()`。

对象的统一管理：程序运行业务逻辑时，能从对象的存储容器中提取到对应的实例`getBean()`。

复杂场景问题：  

Bean存在依赖关系， Bean存在不同的作用域，如何管理Bean的创建、初始化、销毁，

#### Spring IoC使用 官方文档

看源码前，感觉还是把官网IoC相关的说明过一遍，方便看源码时知道应该重点关注哪些部分。
不能奢望把每一块代码都看明白的。

[1. The IoC Container](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans)

1.1. Introduction to the Spring IoC Container and Beans  

介绍了`org.springframework.beans`和`org.springframework.context`是IoC容器的基础；  
两个核心接口`BeanFactory`和`ApplicationContext`。
`ApplicationContext`相对于`BeanFactory`拓展了一些功能：
        
+ 更方便集成AOP
+ 消息资源处理？
+ 事件发布
+ 应用层上下文
  

1.2. Container Overview

+ 配置元数据
    - 基于注解的配置
    - 基于Java的配置
    - 基于XML的配置
+ 实例化容器
+ 使用容器
  

1.3. Bean Overview

+ Bean属性定义
    `class` `name` `scope` `constructor-arg` `property`
    
    ```xml
    <bean id="exampleBean" class="examples.ExampleBean">
        <!-- setter injection using the nested ref element -->
        <property name="beanOne">
            <ref bean="anotherExampleBean"/>
        </property>
        <!-- setter injection using the neater ref attribute -->
        <property name="beanTwo" ref="yetAnotherBean"/>
        <property name="integerProperty" value="1"/>
    </bean>
    ```
+ Bean命名  
    定义别名
    
+ 实例化Beans  
    使用构造方法，使用工厂方法
    

1.4. Dependencies
+ 依赖注入

    运行时根据文档定义将bean依赖的对象的引用通过构造器或setter或接口注入。
    如下：mybean依赖baseBean，mybean实例化的时候会寻砸后baseBean的定义，然后实例化，再通过构造方法传参注入到mybean。
    借助Java的Reflection机制完成。
    ```
    <bean id="myBean" class="top.kwseeker.spring.ioc.bean.MyBean"
            c:content="test" c:baseBean-ref="baseBean"/>
    
    <bean id="baseBean" class="top.kwseeker.spring.ioc.bean.BaseBean"/>
    ```
    
    源码实现 入口是`AbstractBeanFactory`的`doGetBean()`方法，通过构造器注入，`ConstructorResolver`中检查bean依赖，
    然后在`BeanDefinitionValueResolver`中获取被依赖Bean的引用，最终在`SimpleInstantiationStrategy`的`instantiate()`
    方法中实例化myBean。
    
    - 构造器注入  
        XML中通过 constrcutor-arg 传参的话走构造器注入  
    - setter注入  
        XML中通过 property 传参的话走setter注入  
    
    循环依赖问题处理：  
    官方文档上说应该使用setter注入代替构造器注入解决循环依赖的问题。
    
+ 依赖和配置详解
    - 直观的值（基本类型和String类型属性）
    - 引用其他Bean（依赖）
    - 内部Beans
    - 集合
    - Null和空String
    - XML简写（p-namespace、c-namespace）
    - 复合属性

+ depends-on
    用于指定弱依赖（依赖关系不明显，不像ref那种一个Bean包含另一个Bean的引用），
    可能两个Bean只有业务上的依赖，其中一个Bean需要另一个Bean中某个属性值然后才能实例话，
    这种情况就需要使用depends-on属性指定依赖而不是ref。
+ 懒加载Beans
    lazy-init
+ 自动装配模式
    - no (默认通过ref属性手动设定，不写的话默认no)
    - byName（根据Property的Name自动装配，如果一个bean的name，和另一个bean中的Property的name相同，则自动装配这个bean到Property中）
    - byType（根据Property的数据类型（Type）自动装配，如果一个bean的数据类型，兼容另一个bean中Property的数据类型，则自动装配）
    - constructor（根据构造函数参数的数据类型，进行byType模式的自动装配）
    - autodetect（如果发现默认的构造函数，用constructor模式，否则，用byType模式）
    
    基本类型、String、Classes是不能自动装载的，手动设置依赖会覆盖自动装载。
    
    从自动装配中排除Bean。
    
+ 方法注入

    解决不同生命周期的Bean调用时能否得到最新的实例的问题，如Bean A是单例的，Bean B是原型的，A依赖B，
    但是只是在A实例化的时候才会获取B的引用，而B后面是可能有创建新实例的，但是A拿不到。
    
    一种方法是让Bean A实现ApplicationContextAware接口，获取应用上下文，通过getBean获取Bean B的最新实例。
    但是这种方法，使得业务逻辑耦合到Spring框架。
    
    比较干净的做法是使用方法注入（Method Inject），覆盖容器管理的bean上的方法并返回容器中另一个命名bean的查找结果。
    查找通常涉及原型bean。Spring Framework通过使用CGLIB库中的字节码生成来动态生成覆盖该方法的子类来实现此方法注入。
    
    这两种方法的区别在于前者是用户自己实现查询方法，而后者是使用CGLib运行时自动生成并覆盖查询方法。
    
    ```
    //XML配置
    <bean id="commandManager" class="fiona.apple.CommandManager">
        <lookup-method name="createCommand" bean="myCommand"/>
    </bean>
    //注解配置
    @Lookup("myCommand")
    protected abstract Command createCommand(); //Command是依赖的Bean的类型
    ```
    

1.5. Bean Scopes
    
+ singleton（容器内唯一，不过一个应用中一般只有一个容器吧，所以一般和application是差不多的）  
+ prototype（每次都新建）
+ request
+ session
+ application
+ websocket
  

1.6. Customizing the Nature of a Bean（自定义Bean的特性）

+ 生命周期回调

     用到常见的 InitializingBean和DisposableBean接口，可以实现控制IoC容器对Bean生命周期进行管理。
     分别在Bean初始化完成和Bean结构之后运行。
     
+ ApplicationContextAware and BeanNameAware

    这两个接口功能如其名，"aware"是知道的意思，所以ApplicationContextAware意思就是知道ApplicationContext,
    继承这个接口的类会得到一个ApplicationContext的引用。

+ 其他Aware接口
        

1.7. Bean Definition Inheritance

Bean 定义继承: bean 的 parent 属性。

1.8. Container Extension Points

+ BeanPostProcessor
+ BeanFactoryPostProcessor
+ FactoryBean

1.9. Annotation-based Container Configuration

+ @Required
+ @Primary (在多个同类型的Bean中指定首选，对应xml配置bean的primary属性)
+ @Qualifier
+ @Genre
+ @Resource
+ @PostConstruct
+ @PreDestroy

1.10. Classpath Scanning and Managed Components

1.11. Using JSR 330 Standard Annotations
1.12. Java-based Container Configuration

1.13. Environment Abstraction
1.14. Registering a LoadTimeWeaver
1.15. Additional Capabilities of the ApplicationContext
1.16. The BeanFactory

#### Spring IoC API架构

Spring IoC xml配置方式在Spring Boot中已经很少使用了，主要梳理下流程，详细过程参考开头的参考资料就行了。

![](./images/主要接口设计.png)

1. Bean工厂

    + DefaultListableBeanFactory
    
2. Bean资源

    + Resource (I)
    
        用于定位Bean资源；针对不同的Bean资源有不同的实现类，实现类都在 spring-core:org.springframework.core.io 中定义（
        以Resource结尾的class）。
        这个比较好理解，文件和内存都被称做IO。
        
        从类名上看，支持从 字节数组、classpath、? (ClassRelative)、? (Descriptive)、文件系统、文件路径、
        输入流、Url、Vfs 定位资源。
        
        - ClassPathResource (C)
        
            从项目classpath加载bean资源。
            
            ```
            ClassPathResource resource = new ClassPathResource("application-context.xml");
            ```
    
3. Bean定义解析

    ![](./images/IoC%20Bean定义.png)

    + BeanDefinitionReader (I)
    
        - XmlBeanDefinitionReader (C)
        
            * DocumentLoader (I)
            
                * DefaultDocumentLoader
                
                    通过SAX解析XML文档。

    + BeanDefinition (I)
            
      
      
        - RootBeanDefinition (C)

4. Bean实例化与获取

    回到BeanFactory。

注：

*Registry 实现类是IoC容器真正的存储类，里面有集合类用于存取Beans。

#### Spring IoC 容器

包括BeanDefinition的Resource定位、载入、注册。  
IoC容器既是Bean定义reader、工厂又是容器。

+ XMLBeanFactory(最基本的容器实现)

    虽然简单但是却包含资源定位、载入、注册（Bean实例化和保存）这三个基本过程。
    具体工作流程看代码的注释。

+ ApplicationContext（I）

    - XmlWebApplicationContext (C)
    
    - ClasspathXmlApplicationContext (C)

    - FileSystemXmlApplicationContext (C)


#### 注解方式的Bean自动装配的实现

注解方式实现在`spring-context`组件中实现。

注解方式的 `BeanFactory` 对于 Web环境下的最终实现类是 `spring-web` 组件的 `AnnotationConfigWebApplicationContext`；



## IoC 相关问题

+ 三种注入方式及区别

+ BeanFactory与ApplicationContext区别

+ Spring inner beans

+ Bean的作用域与线程安全问题

+ Spring中如何注入一个集合

+ Spring中如何注入Java.util.Properties

+ Spring Bean 的自动装配（依赖查找与依赖注入），有哪5种模式

+ @Required 原理

+ @Autowired 原理

+ @Qualifier 原理

+ @Configuration @Service 与 @Component 什么区别？

    关键是找到各自的注解处理器，先搜索代码查看各自的被引用的位置，@Configuration 被调用位置`AutoConfigurationExcludeFilter.isConfigurtion()`：
    ```
    private boolean isConfiguration(MetadataReader metadataReader) {
        return metadataReader.getAnnotationMetadata().isAnnotated(Configuration.class.getName());
    }
    ```
    加个断点强制执行到这里，然后看调用堆栈信息及代码注释。这是调用堆栈信息：
    ```
    //读取FileSystemResource（如某个配置类）的注解元数据，查看其是否被@Configuration注解
    isConfiguration:53, AutoConfigurationExcludeFilter (org.springframework.boot.autoconfigure)
    //是否被Configuration注解且开启自动装配
    match:49, AutoConfigurationExcludeFilter (org.springframework.boot.autoconfigure)
    //这个FileSystemResource是否被 exclude, 是的话不会走后面的逻辑
    isCandidateComponent:492, ClassPathScanningCandidateComponentProvider (org.springframework.context.annotation)
    //是否是候选组件（即组件是否要自动装配）
    scanCandidateComponents:431, ClassPathScanningCandidateComponentProvider (org.springframework.context.annotation)
    //扫描所有候选组件
    findCandidateComponents:316, ClassPathScanningCandidateComponentProvider (org.springframework.context.annotation)
    doScan:275, ClassPathBeanDefinitionScanner (org.springframework.context.annotation)
    parse:132, ComponentScanAnnotationParser (org.springframework.context.annotation)
    doProcessConfigurationClass:287, ConfigurationClassParser (org.springframework.context.annotation)
    processConfigurationClass:242, ConfigurationClassParser (org.springframework.context.annotation)
    parse:199, ConfigurationClassParser (org.springframework.context.annotation)
    parse:167, ConfigurationClassParser (org.springframework.context.annotation)
    processConfigBeanDefinitions:315, ConfigurationClassPostProcessor (org.springframework.context.annotation)
    postProcessBeanDefinitionRegistry:232, ConfigurationClassPostProcessor (org.springframework.context.annotation)
    invokeBeanDefinitionRegistryPostProcessors:275, PostProcessorRegistrationDelegate (org.springframework.context.support)
    invokeBeanFactoryPostProcessors:95, PostProcessorRegistrationDelegate (org.springframework.context.support)
    invokeBeanFactoryPostProcessors:705, AbstractApplicationContext (org.springframework.context.support)
    refresh:531, AbstractApplicationContext (org.springframework.context.support)
    refresh:140, ServletWebServerApplicationContext (org.springframework.boot.web.servlet.context)
    refresh:742, SpringApplication (org.springframework.boot)
    refreshContext:389, SpringApplication (org.springframework.boot)
    run:311, SpringApplication (org.springframework.boot)
    run:1213, SpringApplication (org.springframework.boot)
    run:1202, SpringApplication (org.springframework.boot)
    main:10, WechatPublicAccountApplication (top.kwseeker.wechat.publicaccount.wechatpublicaccount)
    ```

