package top.kwseeker.spring.java.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 获取 ApplicationContext 有三种方式
 * 1）Autowired
 * 2) 4.3之后新版本特性
 * 3）实现 ApplicationContextAware 接口
 */
@Component
@Order(3)
public class SpringApplicationContextTest implements CommandLineRunner {

    // 1)
    @Autowired
    private ApplicationContext applicationContext;

    private ApplicationContext applicationContext2;

    // 2)
    public SpringApplicationContextTest(ApplicationContext applicationContext) {
        this.applicationContext2 = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        System.err.println("---------------获取应用上下文的三种方式---------------");
        ApplicationContext applicationContext = this.applicationContext;

        System.err.println("1)" + applicationContext);

        System.err.println("2)" + applicationContext2);

        System.err.println("3)" + new ApplicationContextAwareTest().getApplicationContext());
    }

}

// 3) 和第2种方式有什么不同？
@Component
class ApplicationContextAwareTest implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
