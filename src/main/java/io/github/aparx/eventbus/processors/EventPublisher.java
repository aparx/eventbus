package io.github.aparx.eventbus.processors;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Event processor providing a method to publish an {@code Event} instance
 * to accepting subscribers within a {@code ListenerHandle}.
 * <p>Standard implementations are specified in {@link EventProcessors}.
 *
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 28.07.2022
 * @see Event
 * @see ListenerHandle
 * @see EventProcessors
 * @since 1.0
 */
@FunctionalInterface
public interface EventPublisher {

    void publish(@NonNull Event event,
                 @NonNull ListenerHandle scope,
                 @Nullable ErrorHandler errorHandler);

    /**
     * Functional interface used to optionally redirect an exception
     * usually thrown by the invocation of an {@code EventCallback}.
     */
    @FunctionalInterface
    interface ErrorHandler {
        void handle(@NonNull Throwable thrown,
                    @NonNull ListenerHandle scope,
                    @Nullable EventSubscriber<?> sub);
    }
}