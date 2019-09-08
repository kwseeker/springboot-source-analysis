package top.kwseeker.spring.event.listener;

import java.util.EventListener;

public interface TaskPubEventListener extends EventListener {

    void onTaskPubEvent(TaskPubEvent event);
}
