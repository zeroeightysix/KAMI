package me.zeroeightsix.kami.feature

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindFeature(
    /**
     * Find *only* the subtypes of this class. The annotated class itself will not be registered as a feature.
     */
    val findDescendants: Boolean = false
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FindSettings(
    /**
     * Find *only* the subtypes of this class. The annotated class itself will not have its settings registered.
     */
    val findDescendants: Boolean = false,
    /**
     * The settings root node, if `registerSettings` is true
     */
    val settingsRoot: String = ""
)
