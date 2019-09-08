package top.kwseeker.spring.event.simplify;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractApplicationEventMulticaster
        implements ApplicationEventMulticaster {

    //Spring中存储ApplicationListener是分类存储的，每种event类型存储一个子集合类中
    //方便进行类型的范型处理，因为ApplicationListener接口只有一个事件处理方法且是处理所有事件类型
    private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();
    //final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        synchronized (this.applicationListeners) {
            this.applicationListeners.add(listener);
        }
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        synchronized (this.applicationListeners) {
            this.applicationListeners.remove(listener);
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized (this.applicationListeners) {
            this.applicationListeners.clear();
        }
    }

    protected Collection<ApplicationListener<?>> getApplicationListeners() {
        synchronized (this.applicationListeners) {
            return this.applicationListeners;
        }
    }

    //通过eventType查找支持此类型的listener
    //protected Collection<ApplicationListener<?>> getApplicationListeners(
    //        ApplicationEvent event, ResolvableType eventType) {
    //
    //    Object source = event.getSource();
    //    Class<?> sourceType = (source != null ? source.getClass() : null);
    //    ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
    //
    //    // Quick check for existing entry on ConcurrentHashMap...
    //    ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
    //    if (retriever != null) {
    //        return retriever.getApplicationListeners();
    //    }
    //
    //
    //}

    //protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
    //    if (GenericApplicationListener.class.isAssignableFrom(listenerType) ||
    //            SmartApplicationListener.class.isAssignableFrom(listenerType)) {
    //        return true;
    //    }
    //    ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);
    //    return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
    //}
    //
    //protected boolean supportsEvent(
    //        ApplicationListener<?> listener, ResolvableType eventType, @Nullable Class<?> sourceType) {
    //
    //    GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
    //            (GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
    //    return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
    //}
}
