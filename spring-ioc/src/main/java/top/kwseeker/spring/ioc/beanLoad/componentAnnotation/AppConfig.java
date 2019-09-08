package top.kwseeker.spring.ioc.beanLoad.componentAnnotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public WebService webService() {
        System.out.println(">>>>>> AppConfig#webService() ");
        System.out.println(basicService());
        return new WebService(basicService());
    }

    @Bean
    public BasicService basicService() {
        System.out.println(">>>>>> AppConfig#basicService() ");
        return new BasicService();
    }

    private class WebService {
        private AppConfig.BasicService basicService;

        WebService(BasicService basicService) {
            this.basicService = basicService;
        }
    }

    private class BasicService {
    }
}
