package top.kwseeker.hello.service;

import org.springframework.stereotype.Service;

@Service
public class GreetService {

    public GreetService() {
        System.out.println("GreetService construct ...");
    }
}
