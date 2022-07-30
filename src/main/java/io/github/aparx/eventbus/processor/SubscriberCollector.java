package io.github.aparx.eventbus.processor;

import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import io.github.aparx.eventbus.subscriber.SubscriberCollection;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 28.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface SubscriberCollector<E extends EventSubscriber<?>> {
    @NonNull
    SubscriberCollection<?, ? extends E> collect(@NonNull Listener listener);

}