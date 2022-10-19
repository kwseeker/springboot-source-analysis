package top.kwseeker.simplest;

import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.Annotation;

@SpringBootApplication
public class SimplestApplicationTest {

    @Test
    public void test() {
        Annotation[] annotations = SimplestApplicationTest.class.getAnnotations();
    }
}