package top.kwseeker.conditionaldemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.kwseeker.conditionaldemo.bean.DevEnv;
import top.kwseeker.conditionaldemo.bean.LinuxProperty;
import top.kwseeker.conditionaldemo.custom.SimpleLinuxCondition;

@Configuration
@Import(DevLogConfig.class)
public class GlobalConfig {

    public GlobalConfig() {
        System.out.println("globalConfig construct ...");
    }

    @Bean
    public DevEnv devEnv() {
        return new DevEnv();
    }

    //@Bean
    //@ConditionalOnBean(name = "devEnv")
    //public DevLog devLog() {
    //    return new DevLog();
    //}

    @Bean
    @Conditional(SimpleLinuxCondition.class)
    public LinuxProperty linuxProperty() {
        return new LinuxProperty();
    }
}
