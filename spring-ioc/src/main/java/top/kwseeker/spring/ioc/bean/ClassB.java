package top.kwseeker.spring.ioc.bean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "SCOPE_PROTOTYPE")
public class ClassB {

    public void printClass() {
        System.out.println("ClassB: " + this);
    }
}
