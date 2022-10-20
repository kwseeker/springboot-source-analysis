package top.kwseeker.simplest;

import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpringBootApplication
public class SimplestApplicationTest {

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException {
        Method[] methods = SpringBootApplication.class.getDeclaredMethods();
        Annotation annotation = SimplestApplicationTest.class.getDeclaredAnnotation(SpringBootApplication.class);
        for (Method method : methods) {
            Class<?> type = method.getReturnType();
            System.out.println(type == Class.class || type == Class[].class || type.isEnum());
            Object ret = method.invoke(annotation);
            System.out.println(ret);
        }
    }

    @Test
    public void testStandardAnnotationMetadata() {
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(SimplestApplicationTest.class, true);
        boolean annotated = metadata.isAnnotated(Component.class.getName());
        Annotation[] annotations = SimplestApplicationTest.class.getDeclaredAnnotations();
        Class<? extends Annotation> annotationType = annotations[0].annotationType();
        Method[] methods = annotationType.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(method.getParameterCount());
            System.out.println(method.getReturnType());
        }

        //AnnotationTypeMappings.forAnnotationType(annotationType);
        //queue.addLast(new AnnotationTypeMapping(source, annotationType, ann));
    }
}