package me.zeroeightsix.kami.setting

import imgui.ImGui
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberTypeProcessingException
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.RecordSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.RecordConfigType
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.KMutableProperty
import kotlin.reflect.javaType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.mixin.duck.HasSettingInterface

/**
 * Annotation used to mark that a class may have its ConfigType generated at runtime.
 *
 * @param name If not empty, the name that will be used when displaying this type to the user.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GenerateType(val name: String = "") {
    // We use a separate class so exceptions can remain unhandled and trickle up the stack
    @FunctionalInterface
    interface TypeSupplier {
        fun toConfigType(annotatedType: AnnotatedType): ConfigType<*, *, *>?
    }

    companion object {
        var columnMode = false

        @ExperimentalStdlibApi
        fun <T : Any> generateType(
            clazz: Class<T>,
            annotation: GenerateType,
            supplier: TypeSupplier
        ): ConfigType<*, *, *>? {
            val kClass = clazz.kotlin

            val name = annotation.name.ifEmpty { kClass.simpleName!!.toLowerCase() }

            // We pick the constructor with the most amount of parameters.
            // Hopefully this is a constructor that just specifies all the fields in the class - constructors with less parameters might assume defaults, etc.
            val constructor = kClass.constructors.maxByOrNull { it.parameters.size } ?: return null

            // We try to match up constructor parameters to fields in the class.
            // They must have matching names and matching types. If they don't, we throw a tantrum.
            val params = constructor.parameters.mapNotNull {
                @Suppress("NAME_SHADOWING") val name = it.name!!
                // Find the first class member with the same name as the constructor argument
                val member = kClass.members.find { member -> member.name == name }
                if (member == null) {
                    println("Skipping $name in $kClass: please declare it as a `var`, even if intellij complains")
                    return@mapNotNull null
                }

                // If they don't have matching types, throw our tantrum
                if (member.returnType != it.type) throw FiberTypeProcessingException("Constructor $constructor parameter ${it.name} and field ${it.name} must have matching types.")

                // Create the config type from the type of the parameter.
                // I couldn't find a standard way to convert KCallable to an Annotated type, so we use an anonymous object instead
                @Suppress("UNCHECKED_CAST") val type = supplier.toConfigType(
                    object : AnnotatedType {
                        override fun <T : Annotation?> getAnnotation(p0: Class<T>): T? =
                            declaredAnnotations.filterIsInstance(p0).firstOrNull()

                        override fun getAnnotations(): Array<Annotation> = declaredAnnotations
                        override fun getDeclaredAnnotations(): Array<Annotation> = it.annotations.toTypedArray()
                        override fun getType(): Type = it.type.javaType
                    }
                ) as ConfigType<Any, Any, *>?
                if (type == null) {
                    KamiMod.log.error("$it.type (of $name) does not have an already installed config type, it will be skipped for the generation of $kClass's type.")
                    return@mapNotNull null
                }
                KamiConfig.installBaseExtension(type)
                it to (member to type)
            }.toMap()

            val serializableType = RecordSerializableType(
                // Maps the parameters to their serializable type
                params.mapValues { it.value.second.serializedType }.mapKeys { it.key.name!! }
            )
            val configType = RecordConfigType(
                serializableType,
                clazz,
                { map ->
                    constructor.callBy(params.mapValues { it.value.second.toRuntimeType(map[it.key.name!!]) })
                },
                { t ->
                    params.map {
                        @Suppress("NAME_SHADOWING") val name = it.key.name!!
                        val type = it.value.second
                        name to type.toSerializedType(it.value.first.call(t))
                    }.toMap()
                }
            )

            val `interface` = object : SettingInterface<T> {
                override val type: String = name
                override fun valueToString(value: T): String = value.toString()
                override fun valueFromString(str: String): T? {
                    throw InvalidValueException("This type can not be set from a command.")
                }

                override fun displayImGui(name: String, value: T): T? {
                    var dirty = false

                    val next = if (columnMode) {
                        { ImGui.nextColumn() }
                    } else {
                        { }
                    }

                    // Bit of a hack: in column mode, the 'starting' point is expected to be the *second* column. We want labels in the first, so we move to the next column, and hope:
                    // 1. it's the first column
                    // 2. the previous column was empty, thus there is no difference in y.
                    next()

                    params.values.forEach { (member, type) ->
                        @Suppress("NAME_SHADOWING") val value = member.call(value) ?: return@forEach // If null, don't display it.
                        var label = member.name.capitalize()
                        val id = "$name-${member.name}"
                        if (columnMode) {
                            ImGui.text(label)
                            next()
                            label = ""
                        }
                        type.settingInterface?.displayImGui("$label##$id", value)
                            ?.let {
                                if (member is KMutableProperty<*>) {
                                    member.setter.call(value, it)
                                    dirty = true
                                }
                            }
                        next()
                    }
                    next()
                    return if (dirty) value else null
                }
            }

            (configType as HasSettingInterface<*>).settingInterface = `interface`

            return configType
        }
    }
}