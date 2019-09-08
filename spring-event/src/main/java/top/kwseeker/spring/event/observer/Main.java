package top.kwseeker.spring.event.observer;

/**
 * 观察者模式
 *
 * 比如：日志监听代码异常并打印
 * 假如有两个logger，一个监听INFO级别及以上，一个监听ERROR级别及以上
 * 应用中会打印 INFO、NOTICE、WARN、ERROR 四个级别日志
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Logger infoLogger = new Logger();
        Logger errorLogger = new Logger();

        LogType.INFO.getLog().addObserver(infoLogger);
        LogType.NOTICE.getLog().addObserver(infoLogger);
        LogType.WARN.getLog().addObserver(infoLogger);
        LogType.ERROR.getLog().addObserver(infoLogger);
        LogType.ERROR.getLog().addObserver(errorLogger);

        //业务逻辑
        Thread.sleep(2000);
        LogType.INFO.getLog().newLog("occur info log");
        Thread.sleep(2000);
        LogType.NOTICE.getLog().newLog("occur notice log");
        Thread.sleep(2000);
        LogType.ERROR.getLog().newLog("occur error log");
        Thread.sleep(2000);
        LogType.WARN.getLog().newLog("occur warn log");

    }
}
