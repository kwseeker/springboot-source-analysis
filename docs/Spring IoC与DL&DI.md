# Spring IoC与依赖查找、依赖注入

[Spring IoC Container](<https://docs.spring.io/spring/docs/5.0.6.RELEASE/spring-framework-reference/core.html#beans>)

注入即赋值，如下把名字为"restTemplate"的Bean赋值给
restTemplate这个变量。

```
@Autowire
private RestTemplate restTemplate;
```

IoC与DL/DI是相互依赖的。

比如某个Bean内部有另一个类成员变量，那么加载这个Bean要先加载依赖的成员对象，这就需要DL/DI；而DL/DI成员对象又要搜索XML解析获取的BeanDefinition集合（属于IoC概念）。

## Spring IoC的设计思想

#### IoC的目的

1) 处理复杂的对象创建和依赖

Web应用项目对象依赖一般都相当地复杂，每次都重新创建对象进行可能需要创建一堆依赖对象，代码难以维护；IoC目的是由系统自动创建Bean并解决依赖（底层通过外部传入:构造器、setter方法、反射filed.set(obj, x)），统一管理Bean（创建、存储、释放），用户只需要在需要的时候取Bean。

**基于XML IoC具体设计需求**：

+ 1）在配置文件中定义Bean（包括属性值、Bean依赖等）；

+ 2）解析配置文件创建Bean并存储到Map集合；

+ 3）通过Bean的名字查找Map集合获取Bean对象。

需求很简单，但是实现起来却发现没有那么简单（具体可以跟一下ApplicationContext实现类的IoC初始化过程）：

+ **1）首先第一步：在配置文件中定义Bean**

  具体应该怎么定义才能创建出一个Bean（想想构造器）。首先要有**类信息**，然后需要指定**初始化参**数，万一成员也是个Bean，还要把**依赖的Bean**先创建出来，并传到这个Bean定义。有时还会碰到重重依赖或者循环依赖。有时面对某些场景需要创建多个Bean实例。

  ```
  //比如我们实现了两个Bean Class
  UserService {
      private UserDao userDao;
      public void setUserDao(...) {
          ...
      }
      void save() {
          ...
      }
  }
  UserDao {
      void save() {
          ...
      }
  }
  
  //程序员不想管Bean的创建初始化，只想用时能够直接获取，告诉Spring:我给你个XML配置，你管理Bean的生命周期，我用时能取到对象实例就行了
  //XML定义
  <beans>
  	<bean name="userService" class="xxx.UserService">
  		<property name="userDao" ref="userDao">
  		<constructor-arg ... />
  	</bean>
  	<bean name="userDao" class="xxx.UserDao">
  		<property .../>
  		<constructor-arg ... />
  	</bean>
  </beans>
  ```

  

  Bean标签属性：

  

  涉及重要类：

  + **BeanDefinition**

  + **BeanFactoryPostProcessor**

    针对BeanDefinition进行后置处理，对BeanDefintion做修改。

    通过`<context:property-placeholder location="xxx"/>`实现，内部实现加载指定properties文件key-value替换`${}`占位符，如下例子用`jdbc.driverClassName=org.hsqldb.jdbcDriver`替换 `${jdbc.driverClassName}`。

    关于这个标签可以查看官方文档，这里粘贴一下

    ```xml
    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations" value="classpath:com/foo/jdbc.properties"/>
    </bean>
    <bean id="dataSource" destroy-method="close"
            class="org.apache.commons.dbcp.BasicDataSource">
        <property name="driverClassName" value="${jdbc.driverClassName}"/>
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
    ```

    文件`classpath:com/foo/jdbc.properties`

    ```properties
    jdbc.driverClassName=org.hsqldb.jdbcDriver
    jdbc.url=jdbc:hsqldb:hsql://production:9002
    jdbc.username=sa
    jdbc.password=root
    ```

    

+ **2）然后第二步：解析配置文件创建Bean并存储到Map集合**

  ```java
  //然后Spring思考怎么创建Bean
  //有了类和入参，貌似创建Bean实例不难，然后突然发现Bean中有Bean，那么需要先创建被依赖的Bean，就需要去解析XML被依赖的Bean XML节点，创建实例userDao，然后创建userService。
  //如果Bean的依赖层层嵌套就没法玩了，Spring想为何不先将XML的Bean节点先全部解析到一个Map集合，然后后面有被依赖的Bean还没创建的话，直接从这个Map集合查找被依赖的Bean定义并创建实例，然后BeanDefinition就产生了。
  //BeanDefinition集合在哪里？是不是和FactoryBean有关系？
  //BeanDefinition集合和Bean集合都在BeanFactory中。
  
  
  //Bean的实例化（new对象）
  //Bean的属性填充（set方法）
  //Bean的初始化（initMethod方法，Aware接口）
  public class BeanFactory {
      // 存储beanname和单例的bean实例之间的映射关系
      private Map<String,Object> sinletonObjects = new HashMap<>();
  
      // 存储的beanname和BeanDefinition对象的映射关系，该集合的数据是由配置文件加载和解析而来
      private Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();
  
      // 简单工厂设计模式（负担太重、不符合开闭原则）
      public Object getBean(String beanName) {
      	Object bean = sinletonObjects.get(beanName);
      	if(bean != null ) return bean;
  
      	//创建bean对象，然后放入map
  
          // 将要创建的bean的信息通过配置文件（xml）来配置
          // 读取配置文件中的bean信息，然后去创建bean对象
          // 查找beanDefinitionMap集合，获取知道beanname的BeanDefinition对象
          // 通过BeanDefinition对象的bean的信息去创建bean实例
          // 1.实例化---获取class信息，然后通过反射去创建该对象
          // 2.设置属性--获取该bean的PropertyValue集合，去遍历该集合，获取PropertyValue对象中的name和value
          // 需要对value进行处理（需要将字符串值的value，转成指定类型的value）
          // 通过反射去设置value值。
          // 3.初始化---获取bean标签的init-method属性，然后去通过反射，调用实例化对象的指定方法
          sinletonObjects.put(beanname,beanInstance);
      }
  }
  ```

  

  注意基础容器（实现`BeanFactory`接口）是第一使用的时候才创建，高级容器（实现`ApplicationContext`接口）是应用初始化的时候就创建。

  

  - **BeanFactory 和 FactoryBean**

  - **BeanPostProcessor**

    针对Bean进行前后处理，对Bean做修改甚至替换（AOP代理对象产生的地方，在这里用到AOP动态代理）。

    

- **3）最后第三步：通过Bean的名字查找Map集合获取Bean对象**

  

**最终实现流程**：

```txt
1) 获取BeanDefinition；
	1.1）从XML定义获取
		要想让spring帮忙创建bean，必须在xml中告知对方，我们需要的Bean的class全路径（用于使用反射创建Bean实例），Bean的名称（因为创建的Bean最终是要放到Map集合的要用这个名称提取Bean对象）；
		然后可能还需要定义初始化方式（构造器、setter方法），bean的作用区间，别名，懒加载属性，依赖注入等。
		1.1.1）Xml文件读取与解析
	1.2）从注解中读取（TODO）
	
2）解析BeanDefinition创建Bean并存储到Map集合；

3）通过Bean的名字查找Map集合获取Bean对象。

```



## Spring IoC 容器

分为基础容器`BeanFactory`和高级容器`ApplicationContext`，它们都是接口; `BeanFactory`是顶级接口，`ApplicationContext`是其子接口。

### Bean的装载流程

借张图

![IoC Bean装配原理](../images/IoC%20Bean装配原理.png)

####1 Bean的装载方式

除了`FactoryBean`,其他六种方式殊途同归，都有同一个目的即获取`BeanDefinition`。

`FactoryBean`有点特殊，后面分析它的作用时机（TODO）。

参考Demo：/springboot-source-analysis/spring-ioc/src/test/java/top/kwseeker/spring/ioc/SpringIocTest.java

+ **XML**

  基于 `ClassPathXmlApplicationContext`容器。

  其余注解方式基于`AnnotationConfigApplicationContext`容器。

+ **实现FactoryBean**

  `FactoryBean`用于应对复杂的Bean创建逻辑进行自定义Bean的创建过程(比如某些中间件的Bean的加载)，因为可能有某些场景下Bean的定义过于复杂使用Xml或其他方式可能并不容易定义，所以这种情况下倒不如直接用java代码直接写出创建逻辑。

  `FactoryBean`的实现本身也是一个Bean（它也需要被装载，可以通过@Component等方式进行装载），但是它可以生产Bean。

  先放张调用栈，后面看它是怎么整合到上面装载流程的（TODO）

  ```
  getObjectForBeanInstance:1658, AbstractBeanFactory (org.springframework.beans.factory.support)
  getObjectForBeanInstance:1249, AbstractAutowireCapableBeanFactory (org.springframework.beans.factory.support)
  doGetBean:330, AbstractBeanFactory (org.springframework.beans.factory.support)
  getBean:199, AbstractBeanFactory (org.springframework.beans.factory.support)
  getBeansOfType:606, DefaultListableBeanFactory (org.springframework.beans.factory.support)
  postProcessBeanFactory:92, EventListenerMethodProcessor (org.springframework.context.event)
  invokeBeanFactoryPostProcessors:286, PostProcessorRegistrationDelegate (org.springframework.context.support)
  invokeBeanFactoryPostProcessors:181, PostProcessorRegistrationDelegate (org.springframework.context.support)
  invokeBeanFactoryPostProcessors:705, AbstractApplicationContext (org.springframework.context.support)
  refresh:531, AbstractApplicationContext (org.springframework.context.support)
  <init>:88, AnnotationConfigApplicationContext (org.springframework.context.annotation)
  beanLoadByFactoryBeanTest:43, SpringIocTest (top.kwseeker.spring.ioc)
  ```

+ **@Component @ComponentScan**

  包括 @Repository @Service @Controller。

  基于`AnnotationConfigApplicationContext`容器。

  疑问：

  1）@Component 和 @Configuration 的区别？

  2）某个配置类添加@Configuration和不添加的区别？

  带`@Configuration`的话basicService只会创建一次，但是如果不加的话，每次获取basicSerivce都会创建一次。@Configuration原理是通过`ConfigurationClassEnhancer`Cglib动态代理增强截断basicService(), 先去BeanFactory中的单例池找basicService是否存在存在则获取并返回，没有@Configuration的话每次都是执行basicService(),创建一个新的实例。即配置类都是单例。

  具体查看官方文档：`Full @Configuration vs 'lite' @Bean mode`

  ```java
  @Configuration
  public class AppConfig {
  
      @Bean
      public WebService webService() {
          System.out.println(">>>>>> AppConfig#webService() ");
          System.out.println(basicService());
          return new WebService(basicService());
      }
  
      @Bean
      public BasicService basicService() {
          System.out.println(">>>>>> AppConfig#basicService() ");
          return new BasicService();
      }
  
      private class WebService {
          private AppConfig.BasicService basicService;
  
          WebService(BasicService basicService) {
              this.basicService = basicService;
          }
      }
  
      private class BasicService {
      }
  }
  ```

+ **@Bean**

  一般不单独使用。

+ **@Import**

  详细使用参考：[@Import注解的使用](<https://docs.spring.io/spring/docs/5.0.6.RELEASE/spring-framework-reference/core.html#beans-java-using-import>)

  用于从其他配置类中加载Bean定义。

  ```java
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface Import {
  	/**
  	 * {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
  	 * or regular component classes to import.
  	 */
  	Class<?>[] value();
  }
  ```

+ **@ImportResource**

  装载在XML中定义的Bean

+ **@Conditional**

  条件装载，条件类实现`org.springframework.context.annotation.Condition`接口，当matches()返回true，才进行Bean的装载。

#### 2 Bean的注册原理

+ **ClassPathXmlApplicationContext**

  xml装载方式以及xml和注解组合装载方式使用这种。

  ```java
  ClassPathXmlApplicationContext#ClassPathXmlApplicationContext()
  //定位配置文件的位置
  //启动IoC容器
  	AbstractApplicationContext#refresh()
  	//刷新Bean工厂
  		AbstractApplicationContext#obtainFreshBeanFactory()
  		//如果BeanFactory已经存在则关闭
  		//创建 DefaultListableBeanFactory，既是BeanFactory又是一个注册器
  		//BeanFactory定制化：是否允许BeanDefinition覆盖、是否允许循环依赖
  		//加载BeanDefinition
  			AbstractXmlApplicationContext#loadBeanDefinitions()
  			//创建 XmlBeanDefinitionReader
      		//如果配置资源为null，则获取配置文件路径（可以有多个配置资源文件，spring会遍历依次加载），然后使用ResourcePatternResolver获取配置资源
  			//执行真正的加载BeanDefinition逻辑
  				XmlBeanDefinitionReader#doLoadBeanDefinitions()
  				//解析XML文件获取Document对象（？）
      			//获取BeanDefinition注册器
      			//创建BeanDefinitionDocumentReader，使用它注册BeanDefinition
  					DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions(Element root)
      				//创建BeanDefinitionParserDelegate
      				//预处理xml
      				//解析BeanDefintion（依次解析处理每个节点）
      					DefaultBeanDefinitionDocumentReader#parseDefaultElement()
      					//区分import、alias、bean、beans分别处理
      					//bean的处理方式
      						//解析Document节点获取BeanDefinition并包装成BeanDefinitionHolder（除了BeanDefinition属性还有beanName和aliasesArray别名数组）
      						//交给BeanDefinitionReaderUtil处理（拿到Bean的name，注册器：DefaultListableBeanFactory, 使用注册器注册）
      							DefaultListableBeanFactory#registerBeanDefinition()
      								//在 beanDefinitionMap 添加此bean的 name-beanDefinition 的键值对
      								
      				//后置处理xml
  	//... 后面的流程就是Bean的获取流程了
  ```

+ **AnnotationConfigApplicationContext**

  使用注解的Bean装配方式使用这种。

  ```java
  AnnotationConfigApplicationContext#AnnotationConfigApplicationContext()
  //this() 执行父类的构造器和此类的构造器
      GenericApplicationContext#GenericApplicationContext()
      //父类的构造器创建DefaultListableBeanFactory实例 this.beanFactory = new DefaultListableBeanFactory();
      AnnotatedBeanDefinitionReader#AnnotatedBeanDefinitionReader()
      //创建注解形式的BeanDefinition读取器，用于解析并注册BeanDefinition和DefaultBeanDefinitionDocumentReader功能是一样的；
     	//注意这里会默认先注册进去几个Spring的基础Bean
      	AnnotationConfigUtils#registerAnnotationConfigProcessors()
      	//注册 ConfigurationClassPostProcessor
      	//注册 AutowiredAnnotationBeanPostProcessor
      	//注册 CommonAnnotationBeanPostProcessor
      	//注册 EventListenerMethodProcessor
      	//注册 DefaultEventListenerFactory
      ClassPathBeanDefinitionScanner#ClassPathBeanDefinitionScanner()
      //创建Bean定义扫描器
  //注册ApplicationContext传入的配置类（Demo中为AppConfig），作为一个Bean。支持传入多个配置类。
      AnnotationBeanDefinitionReader#doRegisterBean()
      //TODO
  //刷新Bean工厂
      AbstractApplicationContext#obtainFreshBeanFactory()
      //验证BeanFactory是否刷新，然后直接返回 DefaultListableBeanFactory
  	AbstractApplicationContext#prepareBeanFactory()
      	//准备bean工厂： 指定beanFactory的类加载器， 添加后置处理器，注册缺省环境bean等
  		// beanFactory添加了2个后置处理器 ApplicationContextAwareProcessor, ApplicationListenerDetector (new)
      
  ```



#### 3 Bean的获取

​	前面说的几种Bean装配方式只是在BeanDefinition注册的时期处理不同，在获取Bean的时期是一样的。

​	Bean的创建流程源码入口：

​	BeanFactory的getBean()方法。

#### 遗留问题

+ **Autowire 5种模式及原理**

  ```
  AUTOWIRE_NO 默认的方式是不进行自动装配，通过手工设置ref 属性来进行装配bean
  AUTOWIRE_BY_NAME 通过参数名 自动装配，如果一个bean的property和另一个bean的name相同，就自动装配
  AUTOWIRE_BY_TYPE 通过参数的数据类型自动自动装配，如果一个bean的数据类型和另外一个bean的property属性的数据类型兼容，就自动装配
  AUTOWIRE_CONSTRUCTOR 构造方法中的参数通过byType的形式，自动装配
  AUTOWIRE_AUTODETECT 如果有默认的构造方法，通过 construct的方式自动装配，否则使用 byType的方式自动装配。已经废弃
  ```

  `AUTOWIRE_BY_NAME` 和 `AUTOWIRE_BY_TYPE`，默认是使用无参构造方法实例化，通过setter方法装配的？

  `AUTOWIRE_AUTODETECT` 使用参数个数最多的构造方法实例化并赋值。

  

## Bean的依赖注入

#### Bean的依赖查找DL

#### Bean的依赖注入DI



## Bean的生命周期管理

