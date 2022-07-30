package io.github.aparx.eventbus.subscriber.member;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.audience.ListenerHandle;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author aparx (Vinzent Zeband)
 * @version 13:13 CET, 30.07.2022
 * @since 1.0
 */
@FunctionalInterface
interface InvocationArgumentFactory<U extends Event> {

    @Nullable
    Object[] create(ListenerHandle origin, U event);

}