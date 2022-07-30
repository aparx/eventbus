package io.github.aparx.eventbus;

/**
 * @author aparx (Vinzent Zeband)
 * @version 12:03 CET, 30.07.2022
 * @since 1.0
 */
public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
