package top.kwseeker.spring.ioc.beanLoad.importAnnotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
//@Import({MyImportBeanDefinitionRegistrar.class, MyImportSelector.class})
@ImportResource("spring-beanload2.xml")  //导入XML配置Bean
public class AppConfig {

}
