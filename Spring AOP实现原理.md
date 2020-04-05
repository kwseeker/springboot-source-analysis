## Spring AOP 实现原理

#### 1 ApplicationContext 的初始化流程

```
//（TODO）Tomcat加载SpringMvc的原理
ContextLoaderListener#contextInitialized()
	ContextLoader#configureAndRefreshWebApplictionContext()
		AbstractApplicationContext#refresh()	//刷新(新建)BeanFactory
		//创建高级容器的12个步骤（面试点）
		
```

**Spring容器的功能**：

1）提供Bean的管理

2）实现特殊场景的功能Bean（如：JDBCTemplate）

3）对Spring容器中管理的Bean进行功能功能增强（AOP技术）



#### 2 Spring AOP 核心概念和关系

**AOP的起源**：为了拆分业务逻辑和系统逻辑。

**AOP的两种实现**：AOP有两种实现 AspectJ 和 Spring AOP，Spring AOP中又包含两套实现，一个是基于AspectJ pointcuts，一个是Spring auto-proxy。



#### 3 Spring AOP 两种实现的原理

##### 3.1 Spring AOP基于AspectJ pointcuts的实现

##### 3.2 Spring AOP基于auto-proxy的实现



#### 拓展问题

+ @ControllerAdvice 应该也是用AOP实现的，具体实现原理？这是不是一种优雅使用AOP的方式？

