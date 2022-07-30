package io.github.aparx.eventbus.subscriber.member;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.EventCallback;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author aparx (Vinzent Zeband)
 * @version 12:25 CET, 30.07.2022
 * @since 1.0
 */
public interface EventMethodDeducer<T extends Event> {

    EventMethodDeducer<?> FIRST_PARAMETER = newDeducer(Event.class,
            (origin, event) -> new Object[]{event}, 0, Modifier.STATIC);

    boolean isEventMethod(@NonNull Method method);

    @NonNull
    Class<? extends T> getEventType(@NonNull Method method);

    @NonNull
    EventCallback<? super T> newEventCallback(@NonNull Method method);

    /* Global factory methods and variables */

    static <T extends Event> EventMethodDeducer<? extends T> newDeducer(
            final @NonNull Class<? extends T> eventParamBaseType,
            final @NonNull InvocationArgumentFactory<? super T> argsFactory,
            final int eventParamIndex,
            final int disallowedModifiers) {
        Preconditions.checkNotNull(eventParamBaseType);
        Preconditions.checkNotNull(argsFactory);
        return new EventMethodDeducer<>() {

            @Override
            public boolean isEventMethod(@NonNull Method method) {
                if (method.getParameterCount() < 1 + eventParamIndex)
                    return false;
                Class<?> paramType = method.getParameters()[0].getType();
                if (!eventParamBaseType.isAssignableFrom(paramType))
                    return false;
                // In the end we have to confirm that we do not have
                // disallowed modifiers in the `modifiers` bitmask
                return (method.getModifiers() & disallowedModifiers) == 0;
            }

            @Override
            @SuppressWarnings("unchecked")
            public @NonNull Class<? extends T> getEventType(@NonNull Method method) {
                return (Class<? extends T>) method.getParameters()[0].getType();
            }

            @Override
            public @NonNull EventCallback<? super T> newEventCallback(@NonNull Method method) {
                return (origin, event) -> {
                    method.trySetAccessible();
                    method.invoke(origin.getListener(),
                            argsFactory.create(origin, event));
                };
            }
        };
    }

    static <T extends Event> EventMethodDeducer<? extends T> newDeducer(
            final @NonNull Class<? extends T> eventParamBaseType,
            final @NonNull InvocationArgumentFactory<T> argsFactory,
            final int eventParamIndex) {
        // Allocates a new deducer with no disallowed modifiers
        return newDeducer(eventParamBaseType, argsFactory, eventParamIndex, 0);
    }

}