package top.kwseeker.conditionaldemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.kwseeker.conditionaldemo.bean.DevEnv;
import top.kwseeker.conditionaldemo.bean.DevLog;

//@Configuration                        //GlobalConfig中有 @Import(DevLogConfig.class) 就不需这里加@configuration了
//@ConditionalOnBean(name = "devEnv")   //不起效，查不到此名字的BeanDefinition，不能拿一个@Configuration @Bean方法注册的Bean名字或类型作为另一个@Configuration加载的条件
//@ConditionalOnBean(DevEnv.class)      //不起效，查不到此类型的BeanDefinition
@ConditionalOnBean(GlobalConfig.class)  //这种正常
//@ConditionalOnClass(DevEnv.class)       //这种正常，判断类加载器中是否有DevEnv.class, 有则加载
public class DevLogConfig {

    public DevLogConfig() {
        System.out.println("devLogConfig construct ...");
    }

    @Bean
    public DevLog devLog() {
        return new DevLog();
    }
}
