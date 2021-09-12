package top.kwseeker.conditionaldemo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import top.kwseeker.conditionaldemo.bean.DevLog;

@Service
//@ConditionalOnBean(name = "devLog")
//@ConditionalOnBean(DevLog.class)
//@ConditionalOnClass(DevLog.class)
public class LogService {

    public LogService() {
        System.out.println("logService construct ...");
    }
}
