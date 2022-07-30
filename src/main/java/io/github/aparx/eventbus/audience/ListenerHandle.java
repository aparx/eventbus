package io.github.aparx.eventbus.audience;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.subscriber.collection.SubscriberCollection;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Class used to associate a {@code SubscriberCollection} to its respective
 * {@code Listener} instance from where the subscribers result from.
 * <p>As {@code EventSubscriber} implementations are undefined by design,
 * a collection of subscribers or single subscriber may not only be
 * associated to a single listener outside the subscriber itself. An
 * example for this may be a class member subscriber versus a callback
 * subscriber. A callback subscriber would be able to be shared across
 * multiple listener instances, whilst a class member subscriber is bound
 * to its scope, which is the owner in the handle.
 * <p>A {@code ListenerHandle} is primarily used to group a listener
 * instance and {@code SubscriberCollection} with additional declaring
 * meaning.
 *
 * @author aparx (Vinzent Zeband)
 * @version 16:13 CET, 27.07.2022
 * @see Listener
 * @since 1.0
 */
public class ListenerHandle {

    @NonNull
    private final Listener listener;

    @NonNull
    private final SubscriberCollection<?, ?> subscribers;

    public ListenerHandle(@NonNull Listener listener, @NonNull SubscriberCollection<?, ?> subscribers) {
        this.listener = Preconditions.checkNotNull(listener);
        this.subscribers = Preconditions.checkNotNull(subscribers);
    }

    public final @NonNull Listener getOwner() {
        return listener;
    }

    public final @NonNull SubscriberCollection<?, ?> getSubscribers() {
        return subscribers;
    }
}
