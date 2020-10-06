package me.zeroeightsix.kami.util

class ResettableLazy<T>(private var initializer: () -> T) : Lazy<T> {

    private var inner = Inner()

    override val value: T
        get() = inner.value.value

    override fun isInitialized(): Boolean = inner.value.isInitialized()

    fun invalidate() {
        // No need to create a new Inner if no value has been calculated yet
        if (isInitialized())
            inner = Inner()
    }

    private inner class Inner {
        val value = lazy(initializer)
    }
}
