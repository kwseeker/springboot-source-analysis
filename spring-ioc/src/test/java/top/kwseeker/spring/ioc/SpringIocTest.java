package top.kwseeker.spring.ioc;

import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import top.kwseeker.spring.ioc.beanLoad.componentAnnotation.UserService;
import top.kwseeker.spring.ioc.beanLoad.factoryBean.config.AppConfig;
import top.kwseeker.spring.ioc.beanLoad.xml.User;

import java.util.Arrays;

public class SpringIocTest {

    @Test
    public void beanLoadByXmlTest() {
        //使用Spring开发时写法
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-beanload.xml");
        System.out.println(Arrays.toString(applicationContext.getBeanDefinitionNames()));
        User user = (User)applicationContext.getBean("Arvin");
        User user2 = (User)applicationContext.getBean("Arvin");
        //将xml bean标签的 scope 值改为 prototype 再测试
        System.out.println("class=>" + user.toString() + " content=>" + user.contentString());
        System.out.println("class=>" + user2.toString() + " content=>" + user2.contentString());

        //ClassPathXmlApplicationContext内部实现流程
        //1）创建一个简单注册器(内部维持 BeanDefinitionMap ConcurrentHashMap类型， key为Bean的name，value为Bean的BeanDefinition)
        BeanDefinitionRegistry register = new SimpleBeanDefinitionRegistry();
        //2）创建bean 定义读取器
        BeanDefinitionReader reader = new XmlBeanDefinitionReader(register);
        //3) 加载读取xml
        reader.loadBeanDefinitions("spring-beanload.xml");
        System.out.println(Arrays.toString(register.getBeanDefinitionNames()));
        User user1 = (User)applicationContext.getBean("Cindy");
        System.out.println("class=>" + user1.toString() + " content=>" + user1.contentString());
    }

    @Test
    public void beanLoadByFactoryBeanTest() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        //获取MyFactoryBean创建的Bean
        System.out.println(applicationContext.getBean("myFactoryBean"));
        //MyFactoryBean本身也是一个Bean，通过"&"获取MyFactoryBean本身
        System.out.println(applicationContext.getBean("&myFactoryBean"));
    }

    @Test
    public void beanLoadByComponentAnnotationTest() {
        //@Component注解装配使用
        //ApplicationContext applicationContext = new AnnotationConfigApplicationContext(UserService.class);
        //ApplicationContext applicationContext1 = new AnnotationConfigApplicationContext("top.kwseeker.spring.ioc.beanLoad.componentAnnotation");
        //System.out.println(applicationContext.getBean("userService"));
        //System.out.println(applicationContext1.getBean("userService"));

        //测试配置类添加@Configuration和不添加的区别
        ApplicationContext applicationContext2 = new AnnotationConfigApplicationContext(top.kwseeker.spring.ioc.beanLoad.componentAnnotation.AppConfig.class);
        System.out.println(applicationContext2.getBean("basicService"));

        //不加@Configuration， basicService 创建了3次
        //>>>>>> AppConfig#webService()
        //>>>>>> AppConfig#basicService()
        //top.kwseeker.spring.ioc.beanLoad.componentAnnotation.AppConfig$BasicService@587c290d
        //>>>>>> AppConfig#basicService()
        //>>>>>> AppConfig#basicService()
        //top.kwseeker.spring.ioc.beanLoad.componentAnnotation.AppConfig$BasicService@2df32bf7
        //添加@Configuration， basicService 只创建了1次
        //>>>>>> AppConfig#webService()
        //>>>>>> AppConfig#basicService()
        //top.kwseeker.spring.ioc.beanLoad.componentAnnotation.AppConfig$BasicService@3b2da18f
        //top.kwseeker.spring.ioc.beanLoad.componentAnnotation.AppConfig$BasicService@3b2da18f
    }

    @Test
    public void beanLoadByImportAnnotationTest() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(top.kwseeker.spring.ioc.beanLoad.importAnnotation.AppConfig.class);
        //System.out.println(applicationContext.getBean("user"));
        System.out.println(applicationContext.getBean("arvin"));
    }

    @Test
    public void beanLoadByConditionalAnnotationTest() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(top.kwseeker.spring.ioc.beanLoad.conditionalAnnotation.AppConfig.class);
        System.out.println(applicationContext.getBean("dbDao"));
    }
}
