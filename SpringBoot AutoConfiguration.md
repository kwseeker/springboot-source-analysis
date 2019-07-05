# Spring Auto-configuration

[49. Creating Your Own Auto-configuration](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/htmlsingle/#boot-features-developing-auto-configuration)

这部分用于开发Spring第三方共享库自动配置功能。

## 创建自己的Starter

[自定制starter并实现自动配置的Demo](https://github.com/snicoll-demos/spring-boot-master-auto-configuration/blob/master/hornetq-spring-boot-starter/pom.xml)

Mybatis Starter 是一个标准的案例。

#### 完整的 Spring Boot starter 的组成

+ autoconfigure 模块（包含自动配置代码）

    命名规则：module-spring-boot-autoconfigure
    
    Auto-configured Beans (比如 Mybatis 的 MybatisAutoConfiguration) 使用 @Configuration 实现；
    @Conditional等条件注解用于限制何时需要应用自动配置。
    
    [Spring Boot 官方提供的包含众多自动配置的文件 META-INF/spring.factories](https://github.com/spring-projects/spring-boot/blob/v2.1.6.RELEASE/spring-boot-project/spring-boot-autoconfigure/src/main/resources/META-INF/spring.factories)
    
    META-INF/spring.factories
    ```
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
    ```
    
    常用注解（控制自动配置bean加载的条件顺序等）：
    @AutoConfigureAfter  
    @AutoConfigureBefore  
    @AutoConfigureOrder  
    @ConditionalOnBean           仅在当前上下文中存在某个bean时，才会实例化这个Bean。  
    @ConditionalOnClass          某个class位于类路径上，才会实例化这个Bean。  
    @ConditionalOnCloudPlatform  
    @ConditionalOnExpression     某个class位于类路径上，才会实例化这个Bean。  
    @ConditionalOnJava  
    @ConditionalOnJndi  
    @ConditionalOnMissingBean    仅在当前上下文中不存在某个bean时，才会实例化这个Bean。  
    @ConditionalOnMissingClass   仅在当前上下文中不存在某个bean时，才会实例化这个Bean。  
    @ConditionalOnNotWebApplication  不是web应用时才会实例化这个Bean。  
    @ConditionalOnProperty             
    @ConditionalOnResource  
    @ConditionalOnSingleCandidate   
    @ConditionalOnWebApplication  
    
    META-INF/spring-configuration-metadata.json，文档并没有说这个文件怎么生成的，什么用。
    不过看资料得知：这个文件的内容是编译器通过处理所有被@ConfigurationProperties注解的节点生成的，
    就是说这个json文件每个节点对应一个属性；
    查看了下Mybatis的 spring-configuration-metadata.json 和 MybatisProperties 配置项都是对应起来的：
    ```
    // META-INF/spring-configuration-metadata.json
    {
      "sourceType": "org.mybatis.spring.boot.autoconfigure.MybatisProperties",
      "name": "mybatis.config-location",
      "description": "Location of MyBatis xml config file.",
      "type": "java.lang.String"
    },
    // org.mybatis.spring.boot.autoconfigure.MybatisProperties
    @ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
    public class MybatisProperties {
      public static final String MYBATIS_PREFIX = "mybatis";
      private String configLocation;
      ...
    }
    // application.properties
    mybatis.config-location=mybatis-config.xml
    ```
    
    自动配置设置属性：  
    @ConfigurationProperties  
    
    `META-INF/spring-autoconfigure-metadata.properties` 如果该文件存在，则用于过滤不匹配的自动配置，
    这将缩短启动时间。过滤原理参考 `AutoConfigurationImportSelector filter()`。
    
+ starter 模块（包含autoconfigure模块的所有依赖）

    命名规则：module-spring-boot-starter

上面两个模块也可以合并。

#### 定义一个自己的 Starter (hello-spring-boot-starter)

参考 springboot-autoconfigure

## 附录

+ spring.provides 文件的作用

    对于程序本身没有什么用，是给IDE用的，可以用于代码自动补全。







