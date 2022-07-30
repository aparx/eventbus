package io.github.aparx.eventbus.subscriber.collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SortableSubscriberCollection<T, E> newSortableOfMultimap(
            @NonNull MultimapFactory<ListMultimap<Class<? extends T>, E>> factory) {
        return newSortableOfMultimap(factory.newMultimap(), factory, null);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SortableSubscriberCollection<T, E> newSortableOfMultimap(
            @NonNull ListMultimap<Class<? extends T>, E> delegate,
            @NonNull MultimapFactory<ListMultimap<Class<? extends T>, E>> factory) {
        return newSortableOfMultimap(delegate, factory, null);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SortableSubscriberCollection<T, E> newSortableOfMultimap(
            @NonNull MultimapFactory<ListMultimap<Class<? extends T>, E>> factory,
            @Nullable Comparator<? super E> comparator) {
        return newSortableOfMultimap(factory.newMultimap(), factory, comparator);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SortableSubscriberCollection<T, E> newSortableOfMultimap(
            @NonNull ListMultimap<Class<? extends T>, E> delegate,
            @NonNull MultimapFactory<ListMultimap<Class<? extends T>, E>> factory,
            @Nullable Comparator<? super E> comparator) {
        return new SortableCollection<>(delegate, factory, comparator);
    }

    /* Unsorted SubscriberCollection factory methods */

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfMultimapFactory(
            @NonNull MultimapFactory<? extends Multimap<Class<? extends T>, E>> factory) {
        return newOfMultimap(factory.newMultimap(), factory);
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfListMultimap(
            @NonNull ListMultimap<@NonNull Class<? extends T>, @NonNull E> delegate) {
        return newOfMultimap(delegate, MultimapFactory.newUnsortedArraylist());
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfSetMultimap(
            @NonNull SetMultimap<@NonNull Class<? extends T>, @NonNull E> delegate) {
        return newOfMultimap(delegate, MultimapFactory.newUnsortedHashset());
    }

    @NonNull
    public static <T extends Event, E extends EventSubscriber<? extends T>>
    SubscriberCollection<T, E> newOfMultimap(
            @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate,
            @NonNull MultimapFactory<? extends Multimap<Class<? extends T>, E>> multimapFactory) {
        return new BaseCollection<>(delegate, multimapFactory);
    }


    /**
     * Functional factory interface used to generically allocate new
     * {@code Multimap} instances based upon the types given.
     */
    public interface MultimapFactory<T extends Multimap<?, ?>> {
        MultimapFactory<?> UNSORTED_HASHSET = MultimapFactory.typeErased(
                () -> Multimaps.newSetMultimap(new Hashtable<>(), HashSet::new));

        MultimapFactory<?> UNSORTED_ARRAYLIST = MultimapFactory.typeErased(
                () -> Multimaps.newListMultimap(new Hashtable<>(), ArrayList::new));

        @SuppressWarnings("unchecked")
        static <K, V> MultimapFactory<SetMultimap<K, V>> newUnsortedHashset() {
            return (MultimapFactory<SetMultimap<K, V>>) UNSORTED_HASHSET;
        }

        @SuppressWarnings("unchecked")
        static <K, V> MultimapFactory<ListMultimap<K, V>> newUnsortedArraylist() {
            return (MultimapFactory<ListMultimap<K, V>>) UNSORTED_ARRAYLIST;
        }

        static <V extends Multimap<?, ?>>
        MultimapFactory<V> typeErased(@NonNull Supplier<V> factory) {
            Preconditions.checkNotNull(factory);
            return () -> (V) factory.get();
        }

        T newMultimap();

    }

    /* SubscriberCollection class definition */


    // TODO in the future many implementations here might be moved
    //   to a separate public class implementation
    private static class BaseCollection<
            T extends Event,
            E extends EventSubscriber<? extends T>>
            implements SubscriberCollection<T, E> {

        final @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate;
        final @NonNull MultimapFactory<?> mapFactory;

        public BaseCollection(
                @NonNull Multimap<@NonNull Class<? extends T>, @NonNull E> delegate,
                @NonNull MultimapFactory<?> multimapFactory) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.mapFactory = Preconditions.checkNotNull(multimapFactory);
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
            final Multimap<Class<? extends T>, ?> thisMap = asMultimap();
            var out = (Multimap<Class<? extends _T>, _E>) mapFactory.newMultimap();
            // We iterate over every current pair first and determine
            // whether they are type compatible with our query input
            for (var e : thisMap.entries()) {
                if (!baseEventType.isAssignableFrom(e.getKey())) continue;
                if (!baseSubscriberType.isInstance(e.getValue())) continue;
                out.put((Class<? extends _T>) e.getKey(), (_E) e.getValue());
            }
            return new BaseCollection<>(out, mapFactory);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull Collection<? extends E> getGroup(@NonNull Class<? extends Event> eventType) {
            try {
                // First we try and cast the type to the necessary type
                return delegate.get((Class<? extends T>) eventType);
            } catch (Throwable e) { /* discard */ }
            return (Collection<? extends E>) EMPTY_SET;
        }

        @Override
        public @NonNull Collection<E> getGroups() {
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
            return getGroups().iterator();
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
            if (c.isEmpty() ^ isEmpty())
                return false;
            for (Object o : c) {
                if (!contains(o))
                    return false;
            }
            return true;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends E> c) {
            if (c.isEmpty()) return false;
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
            var groups = (Multimap<Class<? extends T>, E>) mapFactory.newMultimap();
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

    private static class SortableCollection<
            T extends Event,
            E extends EventSubscriber<? extends T>>
            extends BaseCollection<T, E>
            implements SortableSubscriberCollection<T, E> {

        @NonNull
        private final Comparator<? super E> baseComparator;

        public SortableCollection(
                @NonNull ListMultimap<@NonNull Class<? extends T>, @NonNull E> map,
                @NonNull MultimapFactory<? extends ListMultimap<Class<? extends T>, E>> multimapFactory,
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
            // O(m*(n log n))
            // Sorts every individual event group
            for (final Class<? extends T> k : delegate.keySet()) {
                ((List<? extends E>) delegate.get(k)).sort(comparator);
            }
        }

    }

}
