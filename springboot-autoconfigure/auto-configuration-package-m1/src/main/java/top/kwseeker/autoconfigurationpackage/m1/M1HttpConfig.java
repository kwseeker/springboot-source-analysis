package top.kwseeker.autoconfigurationpackage.m1;

import org.springframework.context.annotation.Configuration;

@Configuration
public class M1HttpConfig {

    public M1HttpConfig() {
        System.out.println("M1HttpConfig construct ...");
    }
}
