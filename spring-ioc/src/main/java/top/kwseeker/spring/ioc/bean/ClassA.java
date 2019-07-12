package top.kwseeker.spring.ioc.bean;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class ClassA {

    //运行这个注解注释的方法，Spring会使用CGLib动态生成一个getClassB()方法，
    //方法中基于返回值类型调用BeanFactory的实现类的getBean方法返回Bean。
    @Lookup
    public ClassB getClassB() {
        return null;
    }

    public void printClass() {
        System.out.println("ClassA: " + this);
        getClassB().printClass();
    }
}
