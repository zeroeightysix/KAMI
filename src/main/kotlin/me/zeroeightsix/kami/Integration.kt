package me.zeroeightsix.kami

object BaritoneIntegration {

    val present by lazy {
        try {
            Class.forName("baritone.Baritone")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Run `block` if baritone integration is present
     */
    operator fun invoke(block: () -> Unit) {
        if (present) block()
    }

}
