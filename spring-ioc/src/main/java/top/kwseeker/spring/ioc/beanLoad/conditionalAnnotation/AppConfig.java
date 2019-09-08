package top.kwseeker.spring.ioc.beanLoad.conditionalAnnotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        return new DataSource();
    }

    @Bean
    @Conditional(value = MyConditional.class)
    public DbDao dbDao() {
        return new DbDao();
    }
}
