package io.github.aparx.eventbus;

/**
 * Interface marking a class to be able to own, contain and associate
 * {@code EventSubscriber} instances to.
 * <p>A class implementing {@code Listener} is not going to have any
 * side effects through the implementation itself, as the interface is
 * empty. It is purely used to mark classes, often for
 * {@link ListenerRegister} and {@code ListenerHandle}, to be able to
 * safely identify an object supposed to be an owner of subscribers.
 *
 * @author aparx (Vinzent Zeband)
 * @version 16:11 CET, 27.07.2022
 * @see ListenerRegister
 * @see io.github.aparx.eventbus.subscriber.EventSubscriber
 * @see io.github.aparx.eventbus.audience.ListenerHandle
 * @since 1.0
 */
public interface Listener {
}
