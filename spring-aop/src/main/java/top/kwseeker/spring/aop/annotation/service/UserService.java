package top.kwseeker.spring.aop.annotation.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public void queryUserInfo() {
        System.out.println("执行UserService#queryUserInfo()");
    }
}
