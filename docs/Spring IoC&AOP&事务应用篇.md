## Spring IoC、AOP、事务应用篇

内容都比较基础，这里只是总结一下。

#### 1 什么是IoC、DI、AOP、Spring容器满分回答

#### 2 Spring IoC/DI的三种使用方式

##### 2.1 XML

+ 需要掌握`<bean>`标签的各个常用属性的作用

+ 通过`<bean>`标签实例化Bean的三种方式（实例化：Bean的创建但是属性不一定赋值）

  - **构造方法**

    不指定`<constructor-arg>`默认用**无参构造方法**实例化Bean。

    指定`<constructor-arg>`会调用**带参数的构造方法**实例化Bean，并为属性赋值。

  - **静态工厂方法**

    通过指定 `factory-method` 属性。

    ```xml
    <!--StaticFactory为包含静态方法 createUserService 的类-->
    <bean id="userService" class="com.kkb.spring.factory.StaticFactory" factorymethod="createUserService"></bean>
    ```

  - **实例工厂方法**

    通过指定`factory-bean` 、 `factory-method` 属性，需要先创建实例工厂的Bean。

    ```xml
    <!--工厂实例Bean-->
    <bean id="instancFactory" class="com.kkb.factory.InstanceFactory"></bean>
    <!--指定使用的工厂实例Bean的实例工厂方法createUserService-->
    <bean id="userService" factory-bean="instancFactory" factorymethod="
    createUserService"></bean>
    ```

+ DI的实现方式

  DI指Bean的属性依赖，分为简单类型、POJO类型、集合数组类型。

  - **构造方法注入**

    即实例化时的带参数构造方法。

    几个重要的子标签：

    `index`：根据参数索引赋值

     `name`：根据参数名赋值

     `value`：赋值简单类型

     `ref`：赋值引用类型（POJO）

  - **setter方法注入**

    * 子标签`property`

  - **p名称空间注入**

    本质上还是调用 setter 方法。

  DI不同类型的方法

  - 简单类型

  - 引用类型

  - 集合类型

    * List

      ```xml
      <bean id="collectionBean" class="com.kkb.demo5.CollectionBean">
          <property name="arrs">
              <list>
                  <!-- 如果集合内是简单类型，使用value子标签，如果是POJO类型，则使用bean标签 -->
                  <value>美美</value>
                  <value>小风</value>
                  <bean></bean>
              </list>
          </property>
      </bean>
      ```

    * Set

      ```xml
      <property name="sets">
          <set>
              <!-- 如果集合内是简单类型，使用value子标签，如果是POJO类型，则使用bean标签 -->
              <value>哈哈</value>
              <value>呵呵</value>
              <bean></bean>
          </set>
      </property>
      ```

    * Map

      ```xml
      <property name="map">
          <map>
              <entry key="老王2" value="38"/>
              <entry key="凤姐" value="38"/>
              <entry key="如花" value="29"/>
          </map>
      </property>
      ```

    * Properties

      ```xml
      <property name="pro">
          <props>
              <prop key="uname">root</prop>
              <prop key="pass">123</prop>
          </props>
      </property>
      ```

##### 2.2 XML和注解混合

+ spring配置文件需要配置 `context:component-scan`标签，通过`base-package`指定扫瞄的根路径。

+ Bean装载的注解

  - **`@Component`** 或衍生注解 `@Controller` `@Service` `@Repository`

    `@Component`相当于`<bean id="" class="">`, `@Component`注解的value属性与`<bean>`标签的id属性是对应的。

  - **@Bean**
  - **@Import**
  - **@ImportResource**
  - **@Conditional**

+ DI注解

  DI装配Pojo类型分为四种方式，no、byName、byType、constructor。

  - **@Autowired** (byType)

    按类型装配（byType）, 由AutowiredAnnotationBeanPostProcessor类实现。

  - **@Qualifer** (byType的基础上通过bean id区分实现注入)

    默认情况下Bean都是单例的，知道类型基本就知道应该注入哪个Bean，但是有时也可能需要创建多个同类型的Bean，而不同的Bean名字不能相同，这时不能通过byType注入，否则会报存在多个实例的错误。就需要结合Qulifer注解通过byName的方式实现依赖注入。

  - **@Resource**（byName）

  - **@Inject** (byType)

    可以和 **@Name**配合实现byType基础上通过bean id区分注入。

  - **@Value**

    用于注入基本类型和String类型的值。

  改变Bean作用区间的注解

  - **@Scope**

  生命周期相关注解

  - **@PostConstruct**

    相当于bean标签的init-method属性。

  - **@PreDestory**

    相当于bean标签的 destroy-method属性。

##### 2.3 纯注解

+ @Configuration 代替 XML文件, @Bean代替`<bean>`

+ @ComponentScan 代替 `context:component-scan`

+ @PropertySource 代替 `context:property-placeholder`

+ @Import 代替 `<import>`

  具体参考spring boot整合的各个组件的starter。

##### 2.4 XML和注解对比

​	XML是在运行时才被装载读取解析，注解是编译时被装载在运行时被读取解析。

​	所以XML修改后不需要重新编译，注解每次修改都要重新编译，基于这个原因XML比较适合第三方库的Bean（第三方库毕竟一般不能自己去修改代码去编译）；

​	注解配置简单维护方便；

​	综上，给用户使用的Bean使用XML，代码内部Bean的配置使用注解。



#### 3 Spring基于AOP的实现与使用

AOP的作用：在不修改目标类的实现的情况下进行功能增强。

##### 3.1 使用场景（针对不同的模块内拥有相同的功能部分）：

+ 性能检测
+ 权限验证
+ 日志记录
+ 事务控制

##### 3.2 AOP相关术语：

+ Jointpoint (连接点)

  从动态代理的增强原理理解，只可能是类的方法。

+ Pointcut (切入点)

  要进行增强的方法的集合。

+ Advice (通知/增强)

  在Jointpoint前后做的增强，即InvocationHandler中定义的功能，Spring中分为前置通知、后置通知、异常通知、最终通知、环绕通知（TODO：具体是怎么通过动态代理实现的）。

+ Introduction (引介)

  一种特殊的Advice（增强），可以在运行期为类动态地添加一些方法或Field。

+ Target (目标对象)

  被代理的目标对象。

+ Weaving (织入)

  指把增强应用到目标对象来创建新的代理对象的过程。

+ Proxy（代理）

  一个类被AOP织入增强后生成的代理类。

+ Aspect (切面)

  切入点和通知的结合。

+ Advisor (通知器、顾问)

  和Aspect相似。

##### 3.3 AOP的实现方式

+ AspectJ AOP的实现

  编译期静态织入。

+ Spring AOP的实现

  运行时动态织入。

##### 3.4 AOP的使用

+ 5种通知类型

  + **前置通知**（before）

    切入点方法调用前

    `<aop:before method="增强方法名" pointcut-ref="切入点">`

    `@Before(value="切入点")`注释在增强方法上。

  + **后置通知** (after-returning)

    切入点方法正常返回后执行，有异常不会执行。

    `<aop:after-returning method="增强方法名" pointcut-ref="切入点">`

    `@AfterReturning(value="切入点")`注释在增强方法上。

  + **最终通知** (after)

    切入点方法后执行，不管有没有异常都执行。

    `<aop:after method="增强方法名" pointcut-ref="切入点">`

    `@After(value="切入点")`注释在增强方法上。

  + **环绕通知** (around)

    切入点方法之前和之后都会执行。

    `<aop:around method="增强方法名" pointcut-ref="切入点">`

    `@Around(value="切入点")`注释在增强方法上。

    ```java
    //需要额外传入一个参数(待处理的连接点)
    @Around(value="execution(* *.*(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) {
        Object rtValue = null;
        try {
            Object[] args = joinPoint.getArgs();
            //执行前置通知（增强）
            //...(前置增强的代码)
            rtValue = jointPoint.proceed(args);	//执行连接点方法
            //执行后置通知（增强）
            //...（后置增强的代码）
        } catch(Throwable e) {
        } finally {
        }
        return rtValue;
    }
    ```

  + **异常抛出通知** (after-throwing)

    切入点方法执行抛出异常后执行。

    `<aop:after-throwing method="增强方法名" pointcut-ref="切入点">`

    `@AfterThrowing(value="切入点")`注释在增强方法上。

  可以为某个连接点添加上这五种通知，然后使用工具类获取其代理类的java文件看看（TODO）。

+ **Spring + AspectJ**

  + XML方式

    1）编写通知/增强（即增强类）

    2）配置通知的bean, 将通知的bean交给Spring管理

    3）配置AOP切面（包括定义切入点、切入点通知类型、执行的增强方法）

  + XML和注解混合方式

    1）编写切面类（包括切入点、切入点通知类型、对应的增强方法）

    2）开启注解装载Bean扫描

    2）spring配置中开启AOP自动代理

  - 纯注解方式

    使用@ComponentScan代替spring配置中`<context:component-scan base-package="">`

    使用@EnableAspectJAutoProxy代替spring配置中`<aop:aspectj-autoproxy>`

  `<aop:aspectj-autoproxy>`的`proxy-target-class`属性用于配置选择使用哪种动态代理，默认为false表示使用Jdk动态代理，为true表示使用Cglib动态代理；`expose-proxy`属性用于传递代理对象到线程ThreadLocal中，在Spring事务失效时会应用到。

#### 4 Spring声明式事务的使用三种使用方式

