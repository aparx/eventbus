package io.github.aparx.eventbus.processor;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 28.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface EventPublisher {

    void publish(@NonNull Event event,
                 @NonNull ListenerHandle scope,
                 @Nullable ErrorHandler errorHandler);

    @FunctionalInterface
    interface ErrorHandler {
        void handle(@NonNull Throwable thrown,
                    @NonNull ListenerHandle scope,
                    @Nullable EventSubscriber<?> sub);
    }
}