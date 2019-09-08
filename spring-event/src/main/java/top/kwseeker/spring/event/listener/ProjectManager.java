package top.kwseeker.spring.event.listener;


import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 项目经理，任务发布事件的事件源
 */
public class ProjectManager {

    public final Set<TaskPubEventListener> listeners = new LinkedHashSet<>();

    /**
     * 发布任务并执行所有监听器的回调
     */
    public void pubEvent(TaskPubEvent event) {
        for(TaskPubEventListener listener : listeners) {
            listener.onTaskPubEvent(event);
        }
    }

    /**
     * 添加监听者，所有干活的
     */
    public void addListener(TaskPubEventListener listener) {
        listeners.add(listener);
    }
}
