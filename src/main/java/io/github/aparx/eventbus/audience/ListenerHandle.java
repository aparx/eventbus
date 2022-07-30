package io.github.aparx.eventbus.audience;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.subscriber.entities.SubscriberCollection;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 16:13 CET, 27.07.2022
 * @since 1.0
 */
public class ListenerHandle {

    @NonNull
    private final Listener listener;

    @NonNull
    private final SubscriberCollection<?, ?> subscribers;

    public ListenerHandle(
            @NonNull Listener listener,
            @NonNull SubscriberCollection<?, ?> subscribers) {
        this.listener = Preconditions.checkNotNull(listener);
        this.subscribers = Preconditions.checkNotNull(subscribers);
    }

    public final @NonNull Listener getListener() {
        return listener;
    }

    public final @NonNull SubscriberCollection<?, ?> getSubscribers() {
        return subscribers;
    }
}
