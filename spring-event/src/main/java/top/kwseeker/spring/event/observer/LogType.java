package top.kwseeker.spring.event.observer;

public enum LogType {

    INFO(new Log()),
    NOTICE(new Log()),
    WARN(new Log()),
    ERROR(new Log());

    private Log log;

    LogType(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }
}
