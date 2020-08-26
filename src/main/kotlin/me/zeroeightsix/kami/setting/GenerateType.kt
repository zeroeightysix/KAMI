package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberTypeProcessingException
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.RecordSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.RecordConfigType
import me.zeroeightsix.kami.mixin.duck.HasSettingInterface
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import kotlin.reflect.javaType

/**
 * Annotation used to mark that a class may have its ConfigType generated at runtime.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GenerateType {
    // We use a separate class so exceptions can remain unhandled and trickle up the stack
    @FunctionalInterface
    interface TypeSupplier {
        fun toConfigType(annotatedType: AnnotatedType): ConfigType<*, *, *>?
    }

    companion object {
        @ExperimentalStdlibApi
        fun <T : Any> generateType(clazz: Class<T>, supplier: TypeSupplier): ConfigType<*, *, *>? {
            val kClass = clazz.kotlin

            // We pick the constructor with the most amount of parameters.
            // Hopefully this is a constructor that just specifies all the fields in the class - constructors with less parameters might assume defaults, etc.
            val constructor = kClass.constructors.maxByOrNull { it.parameters.size } ?: return null

            // We try to match up constructor parameters to fields in the class.
            // They must have matching names and matching types. If they don't, we throw a tantrum.
            val params = constructor.parameters.map {
                val name = it.name!!
                // Find the first class member with the same name as the constructor argument
                val member = kClass.members.find { it.name == name }!!

                // If they don't have matching types, throw our tantrum
                if (member.returnType != it.type) throw FiberTypeProcessingException("Constructor $constructor parameter ${it.name} and field ${it.name} must have matching types.")

                // Create the config type from the type of the parameter.
                // I couldn't find a standard way to convert KCallable to an Annotated type, so we use an anonymous object instead
                val type = supplier.toConfigType(object : AnnotatedType {
                    override fun <T : Annotation?> getAnnotation(p0: Class<T>): T =
                        declaredAnnotations.filterIsInstance(p0).first()

                    override fun getAnnotations(): Array<Annotation> = declaredAnnotations
                    override fun getDeclaredAnnotations(): Array<Annotation> = it.annotations.toTypedArray()
                    override fun getType(): Type = it.type.javaType
                })
                it to (member to type as ConfigType<Any, Any, *>)
            }.toMap()

            val serializableType = RecordSerializableType(
                // Maps the parameters to their serializable type
                params.mapValues { it.value.second.serializedType }.mapKeys { it.key.name!! }
            )
            val configType = RecordConfigType(serializableType, clazz, { map ->
                constructor.callBy(params.mapValues { it.value.second.toRuntimeType(map[it.key.name!!]) })
            }, { t ->
                params.map {
                    val name = it.key.name!!
                    val type = it.value.second
                    name to type.toSerializedType(it.value.first.call(t))
                }.toMap()
            })

            val interf = object : SettingInterface<T> {
                override val type: String = kClass.simpleName!!.toLowerCase()
                override fun valueToString(value: T): String? = value.toString()
                override fun valueFromString(str: String): T? {
                    throw InvalidValueException("This type can not be set from a command.")
                }

                override fun displayImGui(name: String, t: T): T? {
                    params.values.forEach { (member, type) ->
                        val value = member.call(t) ?: return@forEach // If null, don't display it.
                        type.settingInterface?.displayImGui(member.name.capitalize(), value)
                    }
                    return null
                }
            }

            (configType as HasSettingInterface<T>).settingInterface = interf

            return configType
        }
    }
}
