package top.kwseeker.spring.ioc.beanLoad.conditionalAnnotation;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MyConditional implements Condition {

    /**
     * 定义条件，结果为true才会创建Bean
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if(context.getBeanFactory().containsBean("dataSource")) {
            return true;
        }
        return false;
    }
}
