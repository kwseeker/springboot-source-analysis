# Spring Boot 自动配置原理



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

packages路径注册后哪里使用？要用肯定还要将包路径取出来，但是在取出的方法上加了断点，启动一个空的Spring Boot Web项目并没有执行到断点，为什么？

TODO: 那么@AutoConfigurationPackage导入的包路径什么时候会被用到？主类上也是间接加了这个注解又是怎么处理的？



### 1.2 AutoConfigurationImportSelector

实现了`DeferredImportSelector`接口和一众Aware接口（拥有获取ResourceLoader、Bean容器、环境配置等资源的能力），经过之前的分析`DeferredImportSelector`具有排序、分组的额外处理（**同一分组只有排序最靠前的Bean会被加入`Map<Object, DeferredImportSelectorGrouping> groupings`，也即同一分组中排在第一的配置类才会被加载**，参考 spring-analysis 《Spring JavaConfig.md》）, `AutoConfigurationImportSelector` 定义了以`AutoConfigurationImportSelector.class`作为分组。

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
    //然后去重、过滤(去掉被exclude、不符合filter规则的) 这里去重、过滤后面再研究 TODO
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

1）装载@Component等注解、ImportSelector、ImportSelector 指定的Bean定义，如果里面有嵌套继续1步骤;

2）装载DeferredImportSelector指定的Bean定义

​	  2.1）对DeferredImportSelector按照 是否实现PriorityOrdered接口、@Order、@Priority 进行排序；

​      2.2）对排序后的DeferredImportSelector进行分组，每个分组只包含排在第一位的DeferredImportSelector；

​      2.3）遍历分组，执行分组第一个DeferredImportSelector（先执行process(), 再执行selectImports() ，如果没有自定义DeferredImportSelector.Group，就是以当前DeferredImportSelectorHolder实例作为分组），以AutoConfigurationImportSelector处理为例，先是执行process()遍历所有依赖jar包的 META-INF/spring.factories 读取里面定义的配置类并进行去重过滤，然后执行selectImports() 进行进一步过滤和对配置类进行排序，包装成Group.Entry列表。

​      2.4）最后执行processImports()，如果里面有嵌套引入其他配置，继续1步骤。

TODO : 画个流程图。