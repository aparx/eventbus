package io.github.aparx.eventbus.subscriber;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.audience.ListenerHandle;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 16:11 CET, 27.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface EventCallback<T extends Event> {

    EventCallback<?> EMPTY = (o, e) -> {};

    @NonNull
    @SuppressWarnings("unchecked")
    static <T extends Event> EventCallback<T> empty() {
        return (EventCallback<T>) EMPTY;
    }

    void call(final ListenerHandle origin, final T event) throws Throwable;

    @NonNull
    default EventCallback<T> then(@NonNull EventCallback<? super T> after) {
        Preconditions.checkNotNull(after);
        return (h, e) -> { call(h, e); after.call(h, e); };
    }

}