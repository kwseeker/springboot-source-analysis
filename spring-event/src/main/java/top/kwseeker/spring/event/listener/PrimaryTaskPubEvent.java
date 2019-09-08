package top.kwseeker.spring.event.listener;

public class PrimaryTaskPubEvent extends TaskPubEvent {

    public PrimaryTaskPubEvent(ProjectManager manager) {
        super(manager);
    }

}
