package top.kwseeker.spring.myioc.config;

import java.util.ArrayList;
import java.util.List;

public class BeanDefinition {

    private String clazzName;
    private String beanName;
    private String initMethod;
    private List<PropertyValue> propertyValues = new ArrayList<>();

    //TODO: 拓展更多属性提供更多定制化功能


}
