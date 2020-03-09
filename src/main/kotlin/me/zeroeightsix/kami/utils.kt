package me.zeroeightsix.kami

fun <T, A> T.map(mapper: (T) -> A) = mapper(this)
fun <T> Boolean.to(ifTrue: T, ifFalse: T) = if (this) ifTrue else ifFalse
fun <T> Boolean.then(block: () -> T): T? {
    if (this) return block()
    return null
}
fun <T> Boolean.then(ifTrue: () -> T, ifFalse: () -> T) = if (this) ifTrue() else ifFalse()
fun Boolean.notThen(block: () -> Unit) = (!this).then(block)

/**
 * Returns a resettable, infinitely cycling iterator over this iterable.
 * Please note this will throw an exception if the underlying collection is empty.
 */
fun <T> Iterable<T>.cyclingIterator(): CyclingIterator<T> = CyclingIterator(this)

class CyclingIterator<T>(private val iterable: Iterable<T>) : Iterator<T> {
    var internalIterator = iterable.iterator()

    override fun hasNext(): Boolean = true

    override fun next(): T {
        if (!internalIterator.hasNext()) reset()
        return internalIterator.next()
    }

    fun reset() {
        internalIterator = iterable.iterator()
    }
}