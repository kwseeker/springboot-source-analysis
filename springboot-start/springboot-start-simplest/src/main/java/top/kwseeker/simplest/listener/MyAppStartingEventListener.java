package top.kwseeker.simplest.listener;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

/**
 * 源码可知：启动时会扫 spring.factories 加载监听器
 */
public class MyAppStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent applicationStartingEvent) {
        System.out.println("ApplicationStartingEvent Occur: handle in MyAppStartingEventListener: " + applicationStartingEvent);
    }
}
