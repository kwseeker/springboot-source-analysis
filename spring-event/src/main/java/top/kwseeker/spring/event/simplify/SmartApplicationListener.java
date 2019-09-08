package top.kwseeker.spring.event.simplify;

public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent> {

    /**
     * 判断当前listener是否支持给定的事件类型
     * @param eventType
     * @return
     */
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

    /**
     * 判断当前listener是否支持给定的事件来源
     * @param sourceType
     * @return
     */
    default boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }
}
