package top.kwseeker.spring.event.observer;

import java.util.Observable;

public class Log extends Observable {

    public void newLog(String content) {
        this.setChanged();
        notifyObservers(content);
    }
}
