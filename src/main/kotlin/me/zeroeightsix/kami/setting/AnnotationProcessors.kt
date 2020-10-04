package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.processor.LeafAnnotationProcessor
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigLeafBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute
import net.minecraft.util.Identifier
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

@Target // No target - please use one of the annotations in this class
@Retention(AnnotationRetention.RUNTIME)
annotation class SettingVisibility {
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Constant(val value: Boolean = true)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Method(val value: String)
}

@Target
@Retention(AnnotationRetention.RUNTIME)
annotation class ImGuiExtra {
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Pre(val runner: String)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Post(val runner: String)
}

interface HasId {
    val id: Identifier
}

interface SettingVisibilitySupplier : HasId {
    fun isVisible(): Boolean

    companion object {
        val suppliers = mutableMapOf<Identifier, SettingVisibilitySupplier>()
    }
}

interface SettingImExtraRunner : Runnable, HasId {
    companion object {
        val suppliers = mutableMapOf<Identifier, SettingImExtraRunner>()
    }
}

val visibilityType: StringConfigType<SettingVisibilitySupplier> = createType(SettingVisibilitySupplier::suppliers)
val runnerType: StringConfigType<SettingImExtraRunner> = createType(SettingImExtraRunner::suppliers)

private inline fun <reified T : HasId> createType(suppliers: KProperty0<Map<Identifier, T>>): StringConfigType<T> {
    return ConfigTypes.STRING.derive(
        T::class.java,
        {
            suppliers()[Identifier(it)]
        },
        {
            it!!.id.toString()
        }
    )
}

object ConstantVisibilityAnnotationProcessor : LeafAnnotationProcessor<SettingVisibility.Constant> {
    private val alwaysTrue: SettingVisibilitySupplier = object : SettingVisibilitySupplier {
        override val id: Identifier = Identifier("kami", "visibility_true")
        override fun isVisible() = true
    }

    private val alwaysFalse: SettingVisibilitySupplier = object : SettingVisibilitySupplier {
        override val id: Identifier = Identifier("kami", "visibility_false")
        override fun isVisible() = false
    }

    init {
        SettingVisibilitySupplier.suppliers[alwaysTrue.id] = alwaysTrue
        SettingVisibilitySupplier.suppliers[alwaysFalse.id] = alwaysFalse
    }

    override fun apply(
        annotation: SettingVisibility.Constant?,
        field: Field?,
        pojo: Any?,
        builder: ConfigLeafBuilder<*, *>?
    ) {
        builder!!.withAttribute(
            ConfigAttribute.create(
                FiberId("kami", "setting_visibility"),
                visibilityType,
                if (annotation!!.value) alwaysTrue else alwaysFalse
            )
        )
    }
}

object MethodVisibilityAnnotationProcessor : LeafAnnotationProcessor<SettingVisibility.Method> {
    override fun apply(
        annotation: SettingVisibility.Method?,
        field: Field?,
        pojo: Any?,
        builder: ConfigLeafBuilder<*, *>?
    ) {
        val method = field!!.declaringClass.getDeclaredMethod(annotation!!.value)
        val supplier = object : SettingVisibilitySupplier {
            override val id: Identifier = Identifier("kami", "visibility_method_${method.hashCode()}")
            override fun isVisible(): Boolean = method.invoke(pojo) as Boolean
        }
        SettingVisibilitySupplier.suppliers[supplier.id] = supplier
        builder!!.withAttribute(
            ConfigAttribute.create(
                FiberId("kami", "setting_visibility"),
                visibilityType,
                supplier
            )
        )
    }
}

object ImGuiExtraPostAnnotationProcessor : LeafAnnotationProcessor<ImGuiExtra.Post> {
    override fun apply(annotation: ImGuiExtra.Post?, field: Field?, pojo: Any?, builder: ConfigLeafBuilder<*, *>?) {
        addRunner(pojo, field, annotation!!::runner, builder, "post")
    }
}

object ImGuiExtraPreAnnotationProcessor : LeafAnnotationProcessor<ImGuiExtra.Pre> {
    override fun apply(annotation: ImGuiExtra.Pre?, field: Field?, pojo: Any?, builder: ConfigLeafBuilder<*, *>?) {
        addRunner(pojo, field, annotation!!::runner, builder, "pre")
    }
}

private fun addRunner(
    pojo: Any?,
    field: Field?,
    runner: KProperty0<String>,
    builder: ConfigLeafBuilder<*, *>?,
    age: String,
) {
    val method = field!!.declaringClass.getDeclaredMethod(runner())
    val runner = object : SettingImExtraRunner {
        override val id: Identifier = Identifier("kami", "im_extra_${age}_${method.hashCode()}")
        override fun run() = method.invoke(pojo).let { Unit }
    }
    SettingImExtraRunner.suppliers[runner.id] = runner
    builder!!.withAttribute(
        ConfigAttribute.create(
            FiberId("kami", "im_extra_runner_${age}"),
            runnerType,
            runner
        )
    )
}
