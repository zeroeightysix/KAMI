package me.zeroeightsix.kami

fun <T, A> T.map(mapper: (T) -> A) = mapper(this)
fun <T> Boolean.to(ifTrue: T, ifFalse: T) = if (this) ifTrue else ifFalse
fun <T> Boolean.then(block: () -> T): T? {
    if (this) return block()
    return null
}
fun <T> Boolean.then(ifTrue: () -> T, ifFalse: () -> T) = if (this) ifTrue() else ifFalse()
fun Boolean.notThen(block: () -> Unit) = (!this).then(block)
