# Spring Boot 自动配置原理

目标：

+ 理解Spring IOC 到 Spring Boot 自动配置原理

+ DeferredImportSelector 处理流程，Bean加载顺序排序规则

+ 定制拓展自动配置类

+ 自定义Starter实现自动配置

  

## 1 从@EnableAutoConfiguration看自动配置原理

```java
@SpringBootApplication 
	-> 	@EnableAutoConfiguration
		->  @AutoConfigurationPackage		//将添加该注解的类所在的package作为自动配置package进行管理,这里就是启动类所在包作为自动配置的包
        	-> @Import(AutoConfigurationPackages.Registrar.class)
        	@Import(AutoConfigurationImportSelector.class)
        	-> 
```



### 1.1 AutoConfigurationPackages.Registrar

> `@AutoConfigurationPackage` 是为了给组件开发者使用的，组件开发者在一个路径下面有多个自动配置的类想加载，这个注解就不用对每个类单独添加 @Import 了，直接引入包路径更方便。
>
> `@ComponentScan` 使用组件的开发者准备的，方便你对包路径进行自定义，比如你的一些 Bean 跟 SpringBootApplication 不在一个路径下面，或者多个不同路径下面，这个就起到作用了。
>
> 测试案例: auto-configuration-package-demo
>
> TODO: 两者实现上的差别？工作原理？自动配置包路径有什么用？

```java
class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports 
	registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry)
		register(registry, new PackageImport(metadata).getPackageName());
```

可以看到 `AutoConfigurationPackages.Registrar` 实现了 `ImportBeanDefinitionRegistrar` 接口，将会通过`registerBeanDefinitions()` 导入配置组件。

```java
//packageNames 是从metadata中获取的类所在包的名字
//registry 是Bean容器
public static void register(BeanDefinitionRegistry registry, String... packageNames) {
    if (registry.containsBeanDefinition(BEAN)) {
        BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);
        ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
        constructorArguments.addIndexedArgumentValue(0, addBasePackages(constructorArguments, packageNames));
    }
    else {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(BasePackages.class);
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, packageNames);
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition(BEAN, beanDefinition);
    }
}
```

代码的意思是注册一个名为  `org.springframework.boot.autoconfigure.AutoConfigurationPackages`类型为 `AutoConfigurationPackages.BasePackages` 的 BeanDefinition。
`BasePackages` 通过 ArrayList 存储一组配置包路径。所以Spring Boot 启动后可以通过获取这个Bean实例获取自动配置包路径。

**疑问**：

+ packages路径注册后哪里使用？要用肯定还要将包路径取出来，但是在取出的方法上加了断点，启动一个空的Spring Boot Web项目并没有执行到断点，为什么？

+ @AutoConfigurationPackage导入的包路径什么时候会被用到？

+ 主类上也是间接加了这个注解又是怎么处理的？

  看完完整流程后再回来回答下这个问题：

  主类是通过spring.factories文件，导入外部模块配置类的，貌似确实没用到这个包路径。



### 1.2 AutoConfigurationImportSelector

实现了`DeferredImportSelector`接口和一众Aware接口（拥有获取ResourceLoader、Bean容器、环境配置等资源的能力），经过之前的分析`DeferredImportSelector`具有排序、分组的额外处理（**同一分组只有排序最靠前的Bean会被加入`Map<Object, DeferredImportSelectorGrouping> groupings`，也即同一分组中排在第一的配置类才会被加载**，参考 spring-analysis 《Spring JavaConfig.md》）, `AutoConfigurationImportSelector` 定义了以`AutoConfigurationGroup.class`作为分组。

```java
// AutoConfigurationPackageDemoApplication 这个Demo的分组数据
grouping = {ConfigurationClassParser$DeferredImportSelectorGrouping@4424} 
 group = {AutoConfigurationImportSelector$AutoConfigurationGroup@4374} 
 deferredImports = {ArrayList@4427}  size = 1
  0 = {ConfigurationClassParser$DeferredImportSelectorHolder@4317} 
   configurationClass = {ConfigurationClass@4432} "ConfigurationClass: beanName 'autoConfigurationPackageDemoApplication', top.kwseeker.autoconfigurationpackagedemo.AutoConfigurationPackageDemoApplication"
   importSelector = {AutoConfigurationImportSelector@3692} 
```

由之前《Spring JavaConfig.md》的分析知道，`DeferredImportSelector`（这里具体指` AutoConfigurationImportSelector`）处理流程是先获取Group$Entry列表，即先后调用了分组中的`process()`方法和`selectImports()`方法。

```java
//AutoConfigurationImportSelector$AutoConfigurationGroup
public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
    ...
    //获取所有自动配置元数据（Properties 1K多条，基本都是各个配置类的注解元数据）
    //加载所有候选配置类的全名（load factories from location [META-INF/spring.factories]）
    //然后去重、过滤(去掉被exclude、不符合filter规则的)
    //-> 去重：用LinkedHashSet自身去重的功能，把List先放到Set再把Set放到List就去重了;
    //-> 过滤：1）去掉@EnableAutoConfiguration的两个属性exclude、excludeName指定的类,
    //		  2）获取spring.factories中key为"org.springframework.boot.autoconfigure.AutoConfigurationImportFilter"的value数组集合,
    //	        依次进行规则匹配（比如@OnBeanCondition, @OnClassCondition等）
        //classLoader.getResources("META-INF/spring.factories") 遍历所有引入的依赖jar的 META-INF/spring.factories
        //jar:file:/home/lee/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.1.6.RELEASE/spring-boot-autoconfigure-2.1.6.RELEASE.jar!/META-INF/spring.factories
        //jar:file:/home/lee/.m2/repository/org/springframework/spring-beans/5.1.8.RELEASE/spring-beans-5.1.8.RELEASE.jar!/META-INF/spring.factories
        //...
        //spring.factories 中都是key-> valueArr, 先遍历key,再遍历value数组，将所有配置类存储到LinkedMultiValueMap
    AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
        .getAutoConfigurationEntry(getAutoConfigurationMetadata(), annotationMetadata);
    //autoConfigurationEntries entries
    this.autoConfigurationEntries.add(autoConfigurationEntry);
    for (String importClassName : autoConfigurationEntry.getConfigurations()) {
        this.entries.putIfAbsent(importClassName, annotationMetadata);
    }
}
public Iterable<Entry> selectImports() {
    //如果autoConfigurationEntries为空返回空Entry List
    if (this.autoConfigurationEntries.isEmpty()) {
        return Collections.emptyList();
    }
    
    //将前面所有从spring.factories读取的配置类进行进一步处理,获取最终需要注入的配置类
    //1 获取所有被Exclude的配置类集合A
    Set<String> allExclusions = this.autoConfigurationEntries.stream()
        .map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
    //2 从spring.factories读取的配置类的集合B
    Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
        .map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    //3 从集合B中去除A
    processedConfigurations.removeAll(allExclusions);
	
    //1 先排序（先自然排序[好像是按字符ASCII顺序]、再@Order值排序、最后通过@AutoConfigureBefore\@AutoConfigureAfter注解排序）
    //2 构造成GroupEntry List 返回
    return sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()).stream()
        .map((importClassName) -> new Entry(this.entries.get(importClassName), importClassName))
        .collect(Collectors.toList());
}
```

再后面就是执行`processImports()` **递归**（如果被装载的配置类里面还有@Import等，还会重复上面的流程，不过实现可能有略微差异）装载每个配置类中的配置。

从spring.factories加载的entries：

```
this.entries = {LinkedHashMap@4315}  size = 61
 "org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
  key = "org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration"
  value = {StandardAnnotationMetadata@4313} 
   annotations = {Annotation[2]@4612} 
   nestedAnnotationsAsMap = true
   introspectedClass = {Class@1284} "class top.kwseeker.autoconfigurationpackagedemo.AutoConfigurationPackageDemoApplication"
 "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthIndicatorAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
 "org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration" -> {StandardAnnotationMetadata@4313} 
```

**总结**

1）配置类加载可能是一级一级递归加载的；

2）假如当前进行到某一级配置加载，会根据类型区分是直接加载（如@Component等注解、ImportSelector、ImportSelector）还是延迟加载（DeferredImportSelector），DeferredImportSelector 中指定的配置类是最后加载的。

3）DeferredImportSelector加载配置会先对配置类进行排序和分组，同一分组只有最前面的会被加载。

4）根配置是从所有依赖包的 META-INF/spring.factories 中读取的（读取过程中会去重、过滤）。

**流程**

这里还省略了IoC部分流程。只讨论了从`ConfigurationClassParser$parse`开始的处理流程。

1）装载@Component等注解、ImportSelector、ImportSelector 指定的Bean定义，如果里面有嵌套继续1步骤;

2）装载DeferredImportSelector指定的Bean定义

​	  2.1）对DeferredImportSelector按照 是否实现PriorityOrdered接口、@Order、@Priority 进行排序；

​      2.2）对排序后的DeferredImportSelector进行分组，每个分组只包含排在第一位的DeferredImportSelector；

​      2.3）遍历分组，执行分组第一个DeferredImportSelector（先执行process(), 再执行selectImports() ，如果没有自定义DeferredImportSelector.Group，就是以当前DeferredImportSelectorHolder实例作为分组），以AutoConfigurationImportSelector处理为例，先是执行process()遍历所有依赖jar包的 META-INF/spring.factories 读取里面定义的配置类（SpringBoot刚开始启动时就已经被读取了存在了缓存中，这里只是取下缓存）并进行去重过滤，然后执行selectImports() 对当前分组进行进一步过滤和对配置类进行排序，包装成Group.Entry列表。

​      2.4）最后执行processImports()，如果里面有嵌套引入其他配置，继续1步骤。

贴个别人画的[流程图](https://www.processon.com/view/link/5fc0abf67d9c082f447ce49b)。

> 可以通过在application.properties中添加 debug=true 查看哪些配置类生效。
>
> 会生成一个条件评估报告："CONDITIONS EVALUATION REPORT"。



### 1.3 常用自动配置类原理

外部配置类通常和条件注解一起使用，条件注解充当过滤条件。

下面是2.3.6.RELEASE版本源码。

#### 1.3.1 AopAutoConfiguration

```java
@Configuration(proxyBeanMethods = false)
//spring.aop.auto配置值为true时才加载，如果没有指定值也匹配成功
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

    //有Advice.class才会加载
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Advice.class)
	static class AspectJAutoProxyingConfiguration {

        //使用JDK动态代理配置
		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = false)	
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false",
				matchIfMissing = false)
		static class JdkDynamicAutoProxyConfiguration {
		}

        //使用CGLib动态代理配置
		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = true)	//为何这个注解主类上不用加，因为这里加了
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
				matchIfMissing = true)		//默认使用CGLib
		static class CglibAutoProxyConfiguration {
		}
	}

    //没有org.aspectj.weaver.Advice 且 spring.aop.proxy-target-class 不为false 才会加载
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.aspectj.weaver.Advice")
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	static class ClassProxyingConfiguration {
		ClassProxyingConfiguration(BeanFactory beanFactory) {
			if (beanFactory instanceof BeanDefinitionRegistry) {
				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
		}
	}
}
```



### 1.4 实现功能模块Starter

很久之前写了个hello-spring-boot-starter。官方文档也有教程。

规范一些的流程就是：

1）建个模块工程，实现**业务功能**；

2）建立autoconfigure模块工程，实现**默认的自动配置**（定义配置类，加入到spring.factories文件），比如根据配置决定哪些功能加载哪些不加载；

3）建立starter模块工程，基本就是**编辑pom文件，处理依赖关系**，比如指定依赖哪些jar包，比如autoconfigure。





## 2 自动配置应用

+ **比如引入Mybatis-starter, 默认会注入一个默认的SqlSessionFactory Bean, 但是我们想用自定义的替代默认的，怎么做？**

  首先从前面的源码分析知道默认的Bean的定义是被` AutoConfigurationImportSelector`以`AutoConfigurationGroup`以这个分组加载的。有两种方法：

  1）本身DeferredImportSelector就有最后加载的特性（通常被称为延迟特性），所以使用非DeferredImportSelector方式导入就可以；然后结合条件注解禁止排在后面的同名的Bean的加载；

  2）如果自定义Bean也要用DeferredImportSelector（比如定制了多个实现，实现还有优先级），可以自定义一个分组或多个分组，自定义的Bean定义放到自定义分组，并让自定义分组序号或优先级高于`AutoConfigurationGroup`的序号或优先级；或者所有自定义Bean都放在一个自定义分组中，在分组内排序也可以；总之很灵活。然后结合条件注解禁止排在后面的同名的Bean的加载。

  ```java
  //AutoConfigurationGroup.class分组的排序基本是最后的了
  @Override
  public int getOrder() {
      return Ordered.LOWEST_PRECEDENCE - 1;
  }
  ```

  