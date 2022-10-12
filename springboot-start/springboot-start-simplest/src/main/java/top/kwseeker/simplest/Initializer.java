package top.kwseeker.simplest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("SimplestApplication started ...");
        System.out.println("Now，we can execute our biz ...");

        System.out.println("Finally, exit!");
    }
}
