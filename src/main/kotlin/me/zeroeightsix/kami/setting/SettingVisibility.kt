package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.processor.LeafAnnotationProcessor
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigLeafBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute
import me.zeroeightsix.kami.to
import net.minecraft.util.Identifier
import net.minecraft.util.registry.SimpleRegistry
import java.lang.reflect.Field

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

interface SettingVisibilitySupplier {
    val id: Identifier

    fun isVisible(): Boolean

    object Registry : SimpleRegistry<SettingVisibilitySupplier>()
}

val visibilityType: StringConfigType<SettingVisibilitySupplier> =
    ConfigTypes.STRING.derive(SettingVisibilitySupplier::class.java, {
        SettingVisibilitySupplier.Registry[Identifier(it)]
    }, {
        it!!.id.toString()
    })

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
        SettingVisibilitySupplier.Registry.add(alwaysTrue.id, alwaysTrue)
        SettingVisibilitySupplier.Registry.add(alwaysFalse.id, alwaysFalse)
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
                annotation!!.value.to(alwaysTrue, alwaysFalse)
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
        SettingVisibilitySupplier.Registry.add(supplier.id, supplier)
        builder!!.withAttribute(
            ConfigAttribute.create(
                FiberId("kami", "setting_visibility"),
                visibilityType,
                supplier
            )
        )
    }
}