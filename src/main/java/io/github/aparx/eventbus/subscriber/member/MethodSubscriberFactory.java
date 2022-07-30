package io.github.aparx.eventbus.subscriber.member;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.EventCallback;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;

/**
 * @author aparx (Vinzent Zeband)
 * @version 13:17 CET, 30.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface MethodSubscriberFactory<
        T extends Event,
        R extends ClassMemberEventSubscriber<? extends T, Method>> {

    @NonNull
    R newSubscriber(Class<? extends T> eventType,
                    EventCallback<? super T> callback,
                    Method method);

}
