package io.github.aparx.eventbus;

/**
 * Class {@code Event} is the root of all publishable events whose are
 * applicable to {@code EventCallback} abstractions.
 * <p>The root is necessary, in order for certain event processors to
 * identify whenever the root of an event has been hit. The {@code Event}
 * class itself cannot be instantiated without inheriting it, in order to
 * avoid dummy objects being allocated. The class is also usable in order
 * to provide type-safety without having to rely on third-parties on
 * actually passing the right object. It will hint the developers that a
 * {@code Listener} cannot be an {@code Event} simultaneously, thus
 * creating the possibility for static-analysis-tools to identify an
 * inheritance-meaning-clash.
 *
 * @author aparx (Vinzent Zeband)
 * @version 16:11 CET, 27.07.2022
 * @since 1.0
 */
public class Event {

    protected Event() {
        // Event is supposed to be used as a derived subtype and not
        // allocated as the base class. Thus, the constructor is protected.
    }

}