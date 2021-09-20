package top.kwseeker.hello.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import top.kwseeker.hello.service.HelloService;

/**
 * 自动配置
 */
@Configuration
//添加条件注解
//@Conditional*
//使能配置注解, 相当于把使用 @ConfigurationProperties 的类进行了一次注入
@EnableConfigurationProperties(HelloProperties.class)
@ComponentScan("top.kwseeker.hello.service")    //对于没有在spring.factories中以及没被spring.factories中的配置类注入或扫描的Bean是无法加载的
                                                //可以看看这个注解去掉前后GreetService的构造方法是否会执行
public class HelloAutoConfiguration {

    @Autowired
    private HelloProperties properties;

    //这个是要自动配置生成的bean
    @Bean
    @ConditionalOnMissingBean   //同名bean不存在才创建
    @ConditionalOnProperty(prefix = "hello", value = "enable", havingValue = "true")    //hello.enable为true才创建
    public HelloService getHelloServiceBean() {
        return new HelloService(properties.getName());
    }
}
