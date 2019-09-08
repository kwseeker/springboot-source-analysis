package top.kwseeker.spring.java.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Enumeration;

import static org.springframework.core.io.support.SpringFactoriesLoader.FACTORIES_RESOURCE_LOCATION;

@Component
@Order(2)
public class ClassLoaderResourceTest implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.err.println("---------------获取应用类加载资源---------------");
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);
        Enumeration<URL> sysUrls = ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION);
        System.err.println("Resource: ");
        while(urls.hasMoreElements()) {
            System.err.println(urls.nextElement());
        }
        System.err.println("SystemResources: ");
        while(sysUrls.hasMoreElements()) {
            System.err.println(sysUrls.nextElement());
        }
    }
}
