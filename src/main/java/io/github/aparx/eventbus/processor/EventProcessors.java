package io.github.aparx.eventbus.processor;

import com.google.common.base.Preconditions;
import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.Listener;
import io.github.aparx.eventbus.audience.ListenerHandle;
import io.github.aparx.eventbus.subscriber.EventCallback;
import io.github.aparx.eventbus.subscriber.EventSubscriber;
import io.github.aparx.eventbus.subscriber.EventSubscribers;
import io.github.aparx.eventbus.subscriber.collection.SubscriberCollection;
import io.github.aparx.eventbus.subscriber.collection.SubscriberCollections;
import io.github.aparx.eventbus.subscriber.member.ClassMemberEventSubscriber;
import io.github.aparx.eventbus.subscriber.member.EventMethodDeducer;
import io.github.aparx.eventbus.subscriber.member.MethodSubscriberFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Class {@code EventProcessors} is representing a class of default event
 * processor implementations and factory methods for those.
 *
 * @author aparx (Vinzent Zeband)
 * @version 15:28 CET, 30.07.2022
 * @see EventPublisher
 * @see SubscriberCollector
 * @since 1.0
 */
public final class EventProcessors {

    private EventProcessors() {
        throw new AssertionError();
    }

    /* EventPublisher factory methods */

    @NonNull
    public static EventPublisher newPolymorphicPublisher() {
        return newPublisher(true);
    }

    @NonNull
    public static EventPublisher newPolymorphicPublisher(
            @Nullable Collection<? extends Predicate<? super EventSubscriber<?>>> filters) {
        return newPublisher(true, filters);
    }

    @NonNull
    public static EventPublisher newPublisher(boolean polymorphic) {
        return newPublisher(polymorphic, null);
    }

    /* EventPublisher default implementation */

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EventPublisher newPublisher(
            final boolean polymorphic,
            final @Nullable Collection<? extends Predicate<? super EventSubscriber<?>>> filters) {
        // Allocates a new anonymous publisher implementation
        return (event, scope, errorHandler) -> {
            Preconditions.checkNotNull(event);
            Class<? extends Event> eventType = event.getClass();
            SubscriberCollection<?, ?> col = scope.getSubscribers();
            for (Collection<? extends EventSubscriber<?>> subs; ; ) {
                subs = col.getGroup(eventType);
                l0:
                for (EventSubscriber<?> s : subs) {
                    // Check if `s` is wanted, if not, skip iteration
                    if (filters != null && !filters.isEmpty()) {
                        for (var filter : filters) {
                            if (filter != null && !filter.test(s))
                                continue l0;
                        }
                    }
                    try {
                        // If `event` is not passable to `s`, skip iteration
                        if (!s.isCallablePassingEvent(event)) continue;
                        ((EventSubscriber) s).call(scope, event);
                    } catch (Throwable t) {
                        if (errorHandler == null)
                            throw new RuntimeException(t);
                        // If given, use handler's exception handling
                        errorHandler.handle(t, scope, s);
                    }
                }
                // Continue iterating the inheritance tree if enabled
                if (!polymorphic) break;
                Class<?> spr = eventType.getSuperclass();
                if (spr == Event.class || spr == Object.class) break;
                eventType = (Class<? extends Event>) spr;
            }
        };
    }

    /* SubscriberCollector factory methods */

    private static final Supplier<?> DEFAULT_COLLECTION_FACTORY
            = EventProcessors::newTypeErasedSubscriberCollection;

    @SuppressWarnings("unchecked")
    private static <T extends Event, E extends EventSubscriber<? extends T>>
    Supplier<SubscriberCollection<? super T, E>> getCollectionFactory() {
        return (Supplier<SubscriberCollection<? super T, E>>) DEFAULT_COLLECTION_FACTORY;
    }

    private static SubscriberCollection<?, ?> newTypeErasedSubscriberCollection() {
        return SubscriberCollections.newOfMultimapFactory(
                SubscriberCollections.MultimapFactory.newUnsortedHashset());
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends Event>
    SubscriberCollector<ClassMemberEventSubscriber<T, Method>> newDefaultMethodCollector() {
        var fp = (EventMethodDeducer<? super T>) EventMethodDeducer.FIRST_PARAMETER;
        return newDefaultMethodCollector(fp, null);
    }

    @NonNull
    public static <T extends Event>
    SubscriberCollector<ClassMemberEventSubscriber<T, Method>> newDefaultMethodCollector(
            final @NonNull EventMethodDeducer<? super T> methodDeducer) {
        return newDefaultMethodCollector(methodDeducer, null);
    }

    @NonNull
    public static <T extends Event>
    SubscriberCollector<ClassMemberEventSubscriber<T, Method>> newDefaultMethodCollector(
            final @NonNull EventMethodDeducer<? super T> methodDeducer,
            final @Nullable Collection<? extends Predicate<Method>> filters) {
        return newDefaultMethodCollector(getCollectionFactory(), methodDeducer, filters);
    }

    @NonNull
    public static <T extends Event>
    SubscriberCollector<ClassMemberEventSubscriber<T, Method>> newDefaultMethodCollector(
            final @NonNull Supplier<@NonNull SubscriberCollection<
                    ? super T, ClassMemberEventSubscriber<T, Method>>> collectionFactory,
            final @NonNull EventMethodDeducer<? super T> methodDeducer,
            final @Nullable Collection<? extends Predicate<Method>> filters) {
        // allocates a new collector using the default subscriber factory
        MethodSubscriberFactory<T, ClassMemberEventSubscriber<T, Method>>
                salloc = EventSubscribers::newClassMemberSubscriber;
        // TODO create `salloc` statically in "EventSubscribers" if possible
        return newMethodCollector(collectionFactory, methodDeducer, salloc, filters);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends Event, E extends ClassMemberEventSubscriber<T, Method>>
    SubscriberCollector<E> newMethodCollector(
            final @NonNull Supplier<@NonNull SubscriberCollection<? super T, E>> collectionFactory,
            final @NonNull MethodSubscriberFactory<T, ? extends E> subscriberFactory,
            final @Nullable Collection<? extends Predicate<Method>> filters) {
        var emd = (EventMethodDeducer<? super T>) EventMethodDeducer.FIRST_PARAMETER;
        return newMethodCollector(collectionFactory, emd, subscriberFactory, filters);
    }

    @NonNull
    public static <T extends Event, E extends ClassMemberEventSubscriber<T, Method>>
    SubscriberCollector<E> newMethodCollector(
            final @NonNull EventMethodDeducer<? super T> methodDeducer,
            final @NonNull MethodSubscriberFactory<T, ? extends E> subscriberFactory,
            final @Nullable Collection<? extends Predicate<Method>> filters) {
        return newMethodCollector(getCollectionFactory(),
                methodDeducer, subscriberFactory, filters);
    }

    /* SubscriberCollector default implementation */

    @NonNull
    public static <T extends Event, E extends ClassMemberEventSubscriber<T, Method>>
    SubscriberCollector<E> newMethodCollector(
            final @NonNull Supplier<@NonNull SubscriberCollection<? super T, E>> collectionFactory,
            final @NonNull EventMethodDeducer<? super T> methodDeducer,
            final @NonNull MethodSubscriberFactory<T, ? extends E> subscriberFactory,
            final @Nullable Collection<? extends Predicate<Method>> filters) {
        Preconditions.checkNotNull(collectionFactory);
        Preconditions.checkNotNull(methodDeducer);
        Preconditions.checkNotNull(subscriberFactory);
        return listener -> {
            final Function<Method, ? extends EventCallback<? super T>>
                    pNewEventCallback = methodDeducer::newEventCallback;
            SubscriberCollection<?, E> out = collectionFactory.get();
            Preconditions.checkNotNull(out);
            Class<? extends Listener> type = listener.getClass();
            l0:
            for (Method method : type.getDeclaredMethods()) {
                if (!methodDeducer.isEventMethod(method)) continue;
                if (filters != null && !filters.isEmpty()) {
                    for (var filter : filters) {
                        if (filter != null && !filter.test(method))
                            continue l0;
                    }
                }
                out.add(EventSubscribers.newFactoredMethodSubscriber(methodDeducer,
                        pNewEventCallback, method, subscriberFactory));
            }
            return out;
        };
    }

}
