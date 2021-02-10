package me.zeroeightsix.kami.feature

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindFeature(
    /**
     * Find *only* the subtypes of this class. The annotated class itself will not be registered as a feature.
     */
    val findDescendants: Boolean = false
)