package top.kwseeker.spring.event.listener;

import java.util.EventObject;

/**
 * 开发任务发布事件
 */
public abstract class TaskPubEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final long timestamp;

    public TaskPubEvent(ProjectManager manager) {
        super(manager);
        this.timestamp = System.currentTimeMillis();
    }

    public final long getTimestamp() {
        return this.timestamp;
    }
}
