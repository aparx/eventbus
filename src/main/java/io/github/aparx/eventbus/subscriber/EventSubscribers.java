package io.github.aparx.eventbus.subscriber;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.subscriber.member.ClassMemberEventSubscriber;
import io.github.aparx.eventbus.subscriber.member.EventMethodDeducer;
import io.github.aparx.eventbus.subscriber.member.MethodSubscriberFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:43 CET, 28.07.2022
 * @since 1.0
 */
public final class EventSubscribers {

    private EventSubscribers() {
        throw new AssertionError();
    }

    /* EventSubscriber callback delegating factory methods */

    @NonNull
    public static EventSubscriber<Event> newCallbackSubscriber(
            final @NonNull EventCallback<Event> delegate) {
        return newCallbackSubscriber(Event.class, delegate);
    }

    @NonNull
    public static <T extends Event>
    EventSubscriber<T> newCallbackSubscriber(
            final @NonNull Class<? extends T> eventType,
            final @NonNull EventCallback<? super T> delegate) {
        // Allocates a new anonymous callback subscriber implementation
        Preconditions.checkNotNull(delegate);
        return new EventSubscriber<>(eventType) {
            @Override
            public void call(ListenerHandle origin, T event) throws Throwable {
                delegate.call(origin, event);
            }
        };
    }

    /* ClassMemberEventSubscriber factory methods */

    @NonNull
    public static <U extends Event, V extends Member>
    ClassMemberEventSubscriber<U, V> newClassMemberSubscriber(
            final @NonNull Class<? extends U> eventType,
            final @NonNull EventCallback<? super U> delegate,
            final @NonNull V member) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkNotNull(member);
        // Allocates a new anonymous member subscriber implementation
        return new ClassMemberEventSubscriber<>(eventType) {
            @Override
            public @NonNull V getMember() {
                return member;
            }

            @Override
            public void call(ListenerHandle origin, U event) throws Throwable {
                delegate.call(origin, event);
            }
        };
    }

    @NonNull
    public static <T extends Event>
    ClassMemberEventSubscriber<T, Method> newMethodSubscriber(
            final @NonNull EventMethodDeducer<? super T> deducer,
            final @NonNull Method method) {
        return newMethodSubscriber(deducer, deducer::newEventCallback, method);
    }

    @NonNull
    public static <T extends Event>
    ClassMemberEventSubscriber<T, Method> newMethodSubscriber(
            final @NonNull EventMethodDeducer<? super T> deducer,
            final @NonNull Function<Method, ? extends EventCallback<? super T>> invoker,
            final @NonNull Method method) {
        Preconditions.checkNotNull(invoker);
        return newFactoredMethodSubscriber(deducer, invoker, method,
                (MethodSubscriberFactory<T, ? extends ClassMemberEventSubscriber<T, Method>>)
                        EventSubscribers::newClassMemberSubscriber);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends Event, R extends ClassMemberEventSubscriber<T, Method>>
    R newFactoredMethodSubscriber(
            final @NonNull EventMethodDeducer<? super T> deducer,
            final @NonNull Function<Method, ? extends EventCallback<? super T>> invoker,
            final @NonNull Method method,
            final @NonNull MethodSubscriberFactory<T, ? extends R> factory) {
        Preconditions.checkNotNull(method);
        return factory.newSubscriber(
                (Class<? extends T>) deducer.getEventType(method),
                Preconditions.checkNotNull(invoker.apply(method)),
                method);
    }

}
