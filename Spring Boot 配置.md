# Spring Boot 配置

## 外部化配置

官方也没有明确的定义，暂且认为是在代码外部做的配置，如配置文件、命令行、
环境变量等等，对于代码内部用setter方法设置配置项的值当作内部配置。

### 外部化配置属性源及优先级(从高到低)

1. 开发工具的全局配置属性（~/.spring-boot-devtools.properties, 本人计算机此路径未找到此文件）  
2. 测试属性   
2.1 @TestPropertySource  
2.2 @SpringBootTest  
3. 命令行  
4. 来自SPRING_APPLICATION_JSON的属性（环境变量或系统属性中内嵌的内联JSON）  
5. ServletConfig初始化参数
6. ServletContext初始化参数
7. 来自于java:comp/env的JNDI属性
8. Java系统属性（System.getProperties()）
9. 操作系统环境变量
10. RandomValuePropertySource，只包含random.*中的属性
11. 没有打进jar包的Profile-specific应用属性（application-{profile}.properties和YAML变量）
12. 打进jar包中的Profile-specific应用属性（application-{profile}.properties和YAML变量）
13. 没有打进jar包的应用配置（application.properties和YAML变量）
14. 打进jar包中的应用配置（application.properties和YAML变量）
15. @Configuration类上的@PropertySource注解
16. 默认属性（使用SpringApplication.setDefaultProperties指定）

### 外部化配置属性注入方法

+ @Value
+ Spring Environment 读取
+ @ConfigurationProperties 绑定到结构化对象
