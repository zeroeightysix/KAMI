package me.zeroeightsix.kami.mixin.duck;

/**
 * Indicates that the class can swap something for the duration of a function passed to it.
 * <p>
 * What is being swapped depends on the implementation.
 */
public interface HotSwappable {

    void swapWhile(Runnable runnable);

}
