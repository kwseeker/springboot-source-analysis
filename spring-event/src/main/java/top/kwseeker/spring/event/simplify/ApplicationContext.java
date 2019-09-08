package top.kwseeker.spring.event.simplify;

/**
 * ApplicationContext与SimpleApplicationEventMulticaster是包含关系
 */
public class ApplicationContext {   //ApplicationContext在 Spring 中是接口，此处只是演示事件监听机制定义为类

    private SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();

    public void addApplicationListener(ApplicationListener listener) {
        applicationEventMulticaster.addApplicationListener(listener);
    }
}
