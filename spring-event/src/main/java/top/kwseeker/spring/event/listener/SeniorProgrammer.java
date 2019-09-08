package top.kwseeker.spring.event.listener;

/**
 * 高级程序源监听高级和低级任务发布事件
 */
public class SeniorProgrammer implements TaskPubEventListener {

    @Override
    public void onTaskPubEvent(TaskPubEvent event) {
        if(event.getClass().equals(PrimaryTaskPubEvent.class)) {
            System.out.println("SeniorProgrammer 处理初级任务");
        }
        if(event.getClass().equals(SeniorTaskPubEvent.class)) {
            System.out.println("SeniorProgrammer 处理高级任务");
        }
    }
}
