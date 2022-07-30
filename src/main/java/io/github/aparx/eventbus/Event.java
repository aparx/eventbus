package io.github.aparx.eventbus;

/**
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
