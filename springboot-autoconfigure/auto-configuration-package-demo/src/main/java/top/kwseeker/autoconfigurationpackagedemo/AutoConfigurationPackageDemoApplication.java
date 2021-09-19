package top.kwseeker.autoconfigurationpackagedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import top.kwseeker.autoconfigurationpackage.m1.M1AutoConfiguration;

@SpringBootApplication
@Import(M1AutoConfiguration.class)
public class AutoConfigurationPackageDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationPackageDemoApplication.class, args);
    }

}
