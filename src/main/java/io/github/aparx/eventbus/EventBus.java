package io.github.aparx.eventbus;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.processor.EventPublisher;
import io.github.aparx.eventbus.processor.SubscriberCollector;
import io.github.aparx.eventbus.subscriber.entities.EventSubscriber;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author aparx (Vinzent Zeband)
 * @version 16:10 CET, 27.07.2022
 * @since 1.0
 */
public class EventBus implements ListenerRegister {

    final @NonNull Map<@NonNull Listener, @NonNull ListenerHandle> handleTable;

    final @NonNull EventPublisher publisher;
    final @NonNull SubscriberCollector<?> collector;

    public EventBus(
            @NonNull EventPublisher publisher,
            @NonNull SubscriberCollector<?> collector) {
        // Allocates a new Hashtable as the `handles` field
        this(new Hashtable<>(), publisher, collector);
    }

    protected EventBus(
            @NonNull Map<@NonNull Listener, @NonNull ListenerHandle> handles,
            @NonNull EventPublisher publisher,
            @NonNull SubscriberCollector<?> collector) {
        // Assign each reference to a field of this
        this.handleTable = Preconditions.checkNotNull(handles);
        this.publisher = Preconditions.checkNotNull(publisher);
        this.collector = Preconditions.checkNotNull(collector);
    }

    @Contract(pure = true)
    public final @NonNull EventPublisher getPublisher() {
        return publisher;
    }

    @Contract(pure = true)
    public final @NonNull SubscriberCollector<?> getCollector() {
        return collector;
    }

    @Override
    @Contract(pure = true)
    public final @NonNegative int handleCount() {
        synchronized (handleTable) {
            return handleTable.size();
        }
    }

    public void publish(@NonNull Event event) {
        this.publish(event, null);
    }

    public void publish(@NonNull Event event,
                        EventPublisher.@Nullable ErrorHandler errorHandler) {
        Preconditions.checkNotNull(event);
        synchronized (handleTable) {
            for (ListenerHandle handle : this) {
                // JIT should automatically optimize the repetitive call
                getPublisher().publish(event, handle, errorHandler);
            }
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isRegistered(@NonNull Listener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (handleTable) {
            return handleTable.containsKey(listener);
        }
    }

    @Override
    @CanIgnoreReturnValue
    public boolean register(@NonNull Listener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (handleTable) {
            putHandle(createHandle(listener, getCollector().collect(listener)));
            return true;
        }
    }

    @Override
    @CanIgnoreReturnValue
    public boolean unregister(@NonNull Listener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (handleTable) {
            return handleTable.remove(listener) != null;
        }
    }

    @Override
    public ListenerHandle putHandle(@NonNull ListenerHandle handle) {
        Listener listener = handle.getListener();
        Preconditions.checkNotNull(listener);
        synchronized (handleTable) {
            return handleTable.put(listener, handle);
        }
    }

    @NonNull
    @Override
    @Contract(pure = true)
    public Iterator<ListenerHandle> iterator() {
        synchronized (handleTable) {
            return handleTable.values().iterator();
        }
    }

}