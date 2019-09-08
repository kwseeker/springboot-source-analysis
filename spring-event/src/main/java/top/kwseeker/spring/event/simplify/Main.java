package top.kwseeker.spring.event.simplify;

public class Main {

    //模拟SpringApplication主进程
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();
        context.addApplicationListener(event ->
            System.out.println("触发事件：" + event.getClass().getSimpleName())
        );
    }
}
