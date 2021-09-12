package top.kwseeker.conditionaldemo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
//1 只要存在属性就可以
//@ConditionalOnProperty(name = "spring.profiles.active")
//@ConditionalOnProperty(value = "spring.profiles.active")
//2 属性必须存在且值为dev
//@ConditionalOnProperty(prefix = "spring.profiles", name = "active", havingValue = "dev")
//@ConditionalOnProperty(prefix = "spring.profiles", value = "active", havingValue = "test")
//3 属性值为dev或不存在都匹配
//@ConditionalOnProperty(prefix = "spring.profiles", name = "active", havingValue = "dev", matchIfMissing = true)
//@ConditionalOnProperty(prefix = "spring.profiles", value = "active", havingValue = "dev", matchIfMissing = true)
//4 对于某些复杂条件实在是不方便使用 @ConditionalOnProperty 实现的时候,可使用 @ConditionalOnExpression
@ConditionalOnExpression(
        //这里"：false", 并不是说属性值为false时匹配，而是说没有属性时，默认指定这个属性值为false
        "'${spring.profiles.active}'.equals('dev') and ${condition.open:false}"
        //"'${spring.profiles.active}'.equals('dev') and ${condition.open}"
)
public class PropCondService {

    public PropCondService() {
        System.out.println("PropCondService construct ...");
    }
}
