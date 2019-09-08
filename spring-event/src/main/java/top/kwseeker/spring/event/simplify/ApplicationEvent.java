package top.kwseeker.spring.event.simplify;

import java.util.EventObject;

public abstract class ApplicationEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    //source用于跟踪事件来源
    public ApplicationEvent(Object source) {
        super(source);
    }
}
