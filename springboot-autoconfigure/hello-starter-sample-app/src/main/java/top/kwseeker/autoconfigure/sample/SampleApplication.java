package top.kwseeker.autoconfigure.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.kwseeker.hello.service.HelloService;

@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    @Autowired
    private HelloService helloService;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... args) throws Exception {
        if(helloService != null) {
            System.out.println(helloService.sayHello());
            System.out.println(helloService.sayHello("Lee"));
        } else {
            System.out.println("This bean not exist");
        }
    }
}
