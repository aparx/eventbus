package io.github.aparx.eventbus.processor;

import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import io.github.aparx.eventbus.subscriber.collection.SubscriberCollection;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event processor providing a method that collects all subscribers owned
 * or shared through a {@code Listener} instance.
 * <p>Standard implementations are specified in {@link EventProcessors}.
 *
 * @param <E> The type of {@code EventSubscriber} the collector will
 *            collect and return in its collection.
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 28.07.2022
 * @see Listener
 * @see EventSubscriber
 * @see EventProcessors
 * @since 1.0
 */
@FunctionalInterface
public interface SubscriberCollector<E extends EventSubscriber<?>> {
    @NonNull
    SubscriberCollection<?, E> collect(@NonNull Listener listener);

}