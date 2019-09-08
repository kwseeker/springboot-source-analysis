package top.kwseeker.spring.event.observer;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者，观察被观察对象Observable o，o发生变化则执行update操作。
 * 与其说成观察者不如说是事件处理器，因为观察的行为并不在这里做而是在Observable中做。
 */
public class Logger implements Observer {

    /**
     * 被观察对象发生变化后执行
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("运行日志：" + arg);
    }
}
