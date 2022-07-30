package io.github.aparx.eventbus;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.subscriber.collection.SubscriberCollection;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Interface being a facade with which {@code Listener} instances can be
 * handled using {@code ListenerHandle} implementations.
 * <p>The interface itself does not provide methods that create handles
 * base upon listener instances provided. As mentioned, it is just a facade
 * usually defined and occupied by {@link EventBus}.
 * <p>A {@code ListenerRegister} is iterable over its registered
 * {@link ListenerHandle} instances.
 * <p>The interface does not provide an explicit way of handling
 * concurrency, but the register is meant to be concurrent. In the end, the
 * behaviour of concurrent computation, thus operations, on the register is
 * undefined behaviour.
 *
 * @author aparx (Vinzent Zeband)
 * @version 07:54 CET, 28.07.2022
 * @see EventBus
 * @since 1.0
 */
public interface ListenerRegister extends Iterable<ListenerHandle> {

    @NonNegative
    int handleCount();

    boolean isRegistered(@NonNull Listener listener);

    @CanIgnoreReturnValue
    boolean register(@NonNull Listener listener);

    @CanIgnoreReturnValue
    boolean unregister(@NonNull Listener listener);

    @CanIgnoreReturnValue
    ListenerHandle putHandle(@NonNull ListenerHandle handle);

    default boolean hasHandle(@NonNull ListenerHandle handle) {
        return isRegistered(handle.getOwner());
    }

    default boolean isEmpty() {
        return handleCount() == 0;
    }

    @NonNull
    default ListenerHandle createHandle(
            @NonNull Listener listener,
            @NonNull SubscriberCollection<?, ?> subscribers) {
        return new ListenerHandle(listener, subscribers);
    }

}
