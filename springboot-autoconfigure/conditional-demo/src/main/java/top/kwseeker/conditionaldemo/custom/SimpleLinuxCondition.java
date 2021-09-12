package top.kwseeker.conditionaldemo.custom;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SimpleLinuxCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment environment = conditionContext.getEnvironment();
        // 判断是否是Linux系统, 启动参数添加-Dos.name=linux
        String property = environment.getProperty("os.name");
        if (property == null) {
            return false;
        }
        return property.contains("Linux");
    }
}

