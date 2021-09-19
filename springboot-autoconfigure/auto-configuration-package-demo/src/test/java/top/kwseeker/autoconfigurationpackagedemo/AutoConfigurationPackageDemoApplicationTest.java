package top.kwseeker.autoconfigurationpackagedemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import top.kwseeker.autoconfigurationpackage.m1.M1HttpConfig;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AutoConfigurationPackageDemoApplication.class)
public class AutoConfigurationPackageDemoApplicationTest {

    @Autowired
    private BeanFactory beanFactory;

    @Test
    public void testAutoConfigurationPackages() {
        //获取配置包路径
        //从源码中可以看到，注册了一个名为 "org.springframework.boot.autoconfigure.AutoConfigurationPackages" 类型为 AutoConfigurationPackages$BasePackages 的 BeanDefinition
        //可知最终可以通过获取这个Bean, 获取配置包路径，不过因为 BasePackages 是default内部类, 为了可以取值，AutoConfigurationPackages 类提供了get方法从内部类实例中取包路径
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        //Object basePackagesBean =  beanFactory.getBean("org.springframework.boot.autoconfigure.AutoConfigurationPackages");
        packages.forEach(System.out::println);

        //TODO 为何这个找不到，经调试，SpringBoot启动阶段，并没有执行 AutoConfigurationPackages.get() 方法取包路径（即肯定没有自动装载这个包下的Bean), 那这些包路径何时被用到？
        //M1HttpConfig config = (M1HttpConfig) beanFactory.getBean("m1HttpConfig");
    }
}