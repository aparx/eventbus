package io.github.aparx.eventbus.subscriber;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Event;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:30 CET, 30.07.2022
 * @since 1.0
 */
public interface SortableSubscriberCollection<
        T extends Event,
        E extends EventSubscriber<? extends T>>
        extends SubscriberCollection<T, E> {

    @NonNull
    Comparator<? super E> comparator();

    void sort(@NonNull Comparator<? super E> comparator);

    default void sort() {
        sort(Preconditions.checkNotNull(comparator()));
    }

}
