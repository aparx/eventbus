package io.github.aparx.eventbus.subscriber;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.entities.EventSubscriber;
import io.github.aparx.eventbus.subscriber.entities.SortableSubscriberCollection;
import io.github.aparx.eventbus.subscriber.entities.SubscriberCollection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:54 CET, 30.07.2022
 * @since 1.0
 */
public final class SubscriberCollections {

    private static final Set<?> EMPTY_SET
            = Collections.unmodifiableSet(new HashSet<>());

    private SubscriberCollections() {
        throw new AssertionError();
    }

    /* Sorted SubscriberCollection factory methods */

    /* Unsorted SubscriberCollection factory methods */

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfMultimapFactory(@NonNull MultimapFactory factory) {
        return newOfMultimap(factory.newMap(), factory);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfListMultimap(
            @NonNull ListMultimap<@NonNull Class<? extends T>, @NonNull E> delegate) {
        return newOfMultimap(delegate, MultimapFactory.UNSORTED_ARRAYLIST);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfSetMultimap(
            @NonNull SetMultimap<@NonNull Class<? extends T>, @NonNull E> delegate) {
        return newOfMultimap(delegate, MultimapFactory.UNSORTED_HASHSET);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfMultimap(
            @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate,
            @NonNull MultimapFactory multimapFactory) {
        return new MultimapCollection<>(delegate, multimapFactory);
    }


    /**
     * Functional factory interface used to generically allocate new
     * {@code Multimap} instances based upon the types given.
     */
    public interface MultimapFactory {
        MultimapFactory UNSORTED_HASHSET = MultimapFactory.ofTypeErased(
                () -> Multimaps.newSetMultimap(new Hashtable<>(), HashSet::new));

        MultimapFactory UNSORTED_ARRAYLIST = MultimapFactory.ofTypeErased(
                () -> Multimaps.newListMultimap(new Hashtable<>(), ArrayList::new));

        @NonNull
        static MultimapFactory ofTypeErased(@NonNull Supplier<
                @NonNull Multimap<@NonNull ?, @NonNull ?>> delegate) {
            Preconditions.checkNotNull(delegate);
            return new MultimapFactory() {
                @Override
                @SuppressWarnings("unchecked")
                public <T extends Event, E extends EventSubscriber<? extends T>>
                @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> newMap() {
                    return (Multimap<Class<? extends T>, E>)
                            Preconditions.checkNotNull(delegate.get());
                }
            };
        }

        @NonNull <T extends Event, E extends EventSubscriber<? extends T>>
        Multimap<@NonNull Class<? extends T>, @NonNull E> newMap();
    }

    /* SubscriberCollection class definition */

    public static class MultimapCollection<
            T extends Event,
            E extends EventSubscriber<? extends T>>
            implements SubscriberCollection<T, E> {

        final @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate;
        final @NonNull MultimapFactory multimapFactory;

        public MultimapCollection(
                @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate,
                @NonNull MultimapFactory multimapFactory) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.multimapFactory = Preconditions.checkNotNull(multimapFactory);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull <_T extends T, _E extends EventSubscriber<? extends _T>>
        SubscriberCollection<_T, _E> getDerivedOf(
                final @NonNull Class<? extends _T> baseEventType,
                final @NonNull Class<_E> baseSubscriberType) {
            // Time complexity is roughly equal to O(n)
            Preconditions.checkNotNull(baseEventType);
            Preconditions.checkNotNull(baseSubscriberType);
            Multimap<Class<? extends T>, ?> thisMap = asMultimap();
            Multimap<Class<? extends _T>, _E> outMap = multimapFactory.newMap();
            // We iterate over every current pair first and determine
            // whether they are type compatible with our query input
            for (var e : thisMap.entries()) {
                if (!baseEventType.isAssignableFrom(e.getKey())) continue;
                if (!baseSubscriberType.isInstance(e.getValue())) continue;
                outMap.put((Class<? extends _T>) e.getKey(), (_E) e.getValue());
            }
            return new MultimapCollection<>(outMap, multimapFactory);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull Collection<? extends E> get(@NonNull Class<? extends Event> eventType) {
            try {
                // First we try and cast the type to the necessary type
                return delegate.get((Class<? extends T>) eventType);
            } catch (Throwable e) { /* discard */ }
            return (Collection<? extends E>) EMPTY_SET;
        }

        @Override
        public @NonNull Collection<E> getAll() {
            return delegate.values();
        }

        @Override
        public @NonNull Multimap<@NonNull Class<? extends T>, ? extends E> asMultimap() {
            return delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) return false;
            if (o instanceof Class)
                return delegate.containsKey(o);
            if (o instanceof EventSubscriber)
                return delegate.containsValue(o);
            return false;
        }

        @NonNull
        @Override
        public Iterator<E> iterator() {
            return getAll().iterator();
        }

        @Override
        @CanIgnoreReturnValue
        public boolean add(@NonNull E subscriber) {
            Preconditions.checkNotNull(subscriber);
            return delegate.get(subscriber.getEventType()).add(subscriber);
        }

        @Override
        public boolean remove(@NonNull Object subscriber) {
            if (!(subscriber instanceof EventSubscriber)) return false;
            EventSubscriber<?> sub = (EventSubscriber<?>) subscriber;
            return delegate.remove(sub.getEventType(), sub);
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {
            for (Object o : c) {
                if (!contains(o))
                    return false;
            }
            return true;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends E> c) {
            boolean rv = false;
            for (E e : c) {
                rv |= add(e);
            }
            return rv;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            return batchRemove(this, true, c.iterator());
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            // If any element in this collection is not contained in `c`,
            // it is scheduled for removal identified by its event group.
            return batchRemove(c, false, this.iterator());
        }

        @Override
        @CanIgnoreReturnValue
        public boolean removeAll(@NonNull Class<? extends T> eventType) {
            Preconditions.checkNotNull(eventType);
            return !delegate.removeAll(eventType).isEmpty();
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @SuppressWarnings("unchecked")
        private boolean batchRemove(Collection<?> lCon, boolean cmp, Iterator<?> rItr) {
            // Multimap to batch remove subscribers in their respective
            // group. This will boost performance drastically if many
            // elements are removed, as each individual group is batch
            // removed.
            Multimap<Class<? extends T>, E> groups = multimapFactory.newMap();
            while (rItr.hasNext()) {
                Object rhs = rItr.next();
                if (lCon.contains(rhs) == cmp) {
                    final E e = (E) rhs;
                    groups.put(e.getEventType(), e);
                }
            }
            if (groups.isEmpty()) return false;
            boolean rv = false;
            // Iterate over the groups scheduled for removal and batch
            // remove their contained values within this map.
            Map<Class<? extends T>, Collection<E>> map = groups.asMap();
            for (Map.Entry<Class<? extends T>, Collection<E>> e : map.entrySet()) {
                if (!delegate.containsKey(e.getKey())) continue;
                Collection<E> v = delegate.get(e.getKey());
                rv |= v.removeAll(e.getValue());
            }
            return rv;
        }

        @Override
        public String toString() {
            return Objects.toString(delegate);
        }
    }

    private static class SortedImpl<
            T extends Event,
            E extends EventSubscriber<? extends T>>
            extends MultimapCollection<T, E>
            implements SortableSubscriberCollection<T, E> {

        @NonNull
        private final Comparator<? super E> baseComparator;

        public SortedImpl(
                @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> map,
                @NonNull MultimapFactory multimapFactory,
                @Nullable Comparator<? super E> comparator) {
            super(map, multimapFactory);
            this.baseComparator = comparator == null
                    ? Ordering.natural() : comparator;
        }

        @Override
        public @NonNull Comparator<? super E> comparator() {
            return baseComparator;
        }

        @Override
        public void sort(@NonNull Comparator<? super E> comparator) {

        }

    }

}
