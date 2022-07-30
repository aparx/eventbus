package io.github.aparx.eventbus.subscriber;

import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import io.github.aparx.eventbus.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:08 CET, 28.07.2022
 * @since 1.0
 */
public interface SubscriberCollection<
        T extends Event,
        E extends EventSubscriber<? extends T>>
        extends Collection<E> {

    // <= O(2n)
    @Contract(pure = true)
    @NonNull <_T extends T, _E extends EventSubscriber<? extends _T>>
    SubscriberCollection<_T, _E> getDerivedOf(
            @NonNull Class<? extends _T> baseEventType,
            @NonNull Class<_E> baseSubscriberType);

    // <= O(log n)
    @NonNull
    @Contract(pure = true)
    Collection<@NonNull ? extends E> getGroup(@NonNull Class<? extends Event> eventType);

    // <= O(n)
    @Contract(pure = true)
    @NonNull Collection<@NonNull E> getGroups();

    // <= O(1)
    @Contract(pure = true)
    @NonNull Multimap<@NonNull Class<? extends T>, @NonNull ? extends E> asMultimap();

    @CanIgnoreReturnValue
    boolean add(@NonNull E subscriber);

    @CanIgnoreReturnValue
    boolean remove(@NonNull Object subscriber);

    @CanIgnoreReturnValue
    boolean removeAll(@NonNull Class<? extends T> eventType);

    @CanIgnoreReturnValue
    void clear();

    @NonNull
    @Contract(pure = true)
    default Collection<@NonNull ? extends E> getGroup(@CompatibleWith("T") @NonNull Event event) {
        // This method is essentially existing to hint third parties on
        // static code analysis that `event` can be of any type, but to
        // get a valid result it needs to be of `T` as this collection
        // only stores subscribers with an event-type type-compatible
        // with `T`.
        return getGroup(event.getClass());
    }

    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    @Override
    default Object[] toArray() {
        return getGroups().toArray();
    }

    @NonNull
    @Override
    default <T> T[] toArray(@NonNull T[] a) {
        return getGroups().toArray(a);
    }

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.SORTED);
    }
}