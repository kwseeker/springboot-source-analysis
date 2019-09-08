package top.kwseeker.spring.event.listener;

/**
 * 事件/监听器模式, 之前一直以为事件监听器是基于低耦合的主动监听，看到真正的实现流程总是会感到实现原来这么low
 *
 * 程序员监听项目经理发布的任务并处理
 */
public class Main {

    public static void main(String[] args) {
        ProjectManager manager = new ProjectManager();
        PrimaryProgrammer primaryProgrammer = new PrimaryProgrammer();
        SeniorProgrammer seniorProgrammer = new SeniorProgrammer();

        manager.addListener(primaryProgrammer);
        manager.addListener(seniorProgrammer);

        manager.pubEvent(new PrimaryTaskPubEvent(manager));
        manager.pubEvent(new SeniorTaskPubEvent(manager));
    }
}
