package top.kwseeker.spring.event.listener;

/**
 * 初级程序源监听初级任务发布事件
 */
public class PrimaryProgrammer implements TaskPubEventListener {

    /**
     * 当初级任务发布后调用
     */
    @Override
    public void onTaskPubEvent(TaskPubEvent event) {
        if(event.getClass().equals(PrimaryTaskPubEvent.class)) {
            System.out.println("PrimaryProgrammer 处理初级任务");
        }
    }
}
