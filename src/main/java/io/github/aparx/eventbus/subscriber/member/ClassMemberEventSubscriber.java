package io.github.aparx.eventbus.subscriber.member;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Member;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 30.07.2022
 * @since 1.0
 */
public abstract class ClassMemberEventSubscriber<
        U extends Event, V extends Member>
        extends EventSubscriber<U> {

    public ClassMemberEventSubscriber(@NonNull Class<? extends U> eventType) {
        super(eventType);
    }

    @NonNull
    abstract public V getMember();

}
