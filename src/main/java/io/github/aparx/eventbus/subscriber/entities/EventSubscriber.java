package io.github.aparx.eventbus.subscriber.entities;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CompatibleWith;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.EventCallback;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

/**
 * @author aparx (Vinzent Zeband)
 * @version 16:11 CET, 27.07.2022
 * @since 1.0
 */
public abstract class EventSubscriber<T extends Event>
        implements EventCallback<T>, Comparable<T> {

    @NonNull
    private final Class<? extends T> eventType;

    public EventSubscriber(@NonNull Class<? extends T> eventType) {
        this.eventType = Preconditions.checkNotNull(eventType);
    }

    @Contract("null->false")
    public boolean isCallablePassingEvent(@Nullable Event event) {
        return eventType.isInstance(event);
    }

    public final @NonNull Class<? extends T> getEventType() {
        return eventType;
    }

    @Override
    public int compareTo(@NonNull T o) {
        return 0;
    }
}
