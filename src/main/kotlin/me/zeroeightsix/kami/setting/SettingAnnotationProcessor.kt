package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.processor.LeafAnnotationProcessor
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigLeafBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute
import me.zeroeightsix.kami.setting.KamiConfig.typeMap
import net.minecraft.util.Identifier
import java.lang.reflect.Field

object SettingAnnotationProcessor : LeafAnnotationProcessor<Setting> {

    val INTERFACE_TYPE: StringConfigType<SettingInterface<*>> =
        ConfigTypes.STRING.derive(SettingInterface::class.java,
            {
                SettingInterface.Registry[Identifier(it)]
            },
            {
                it!!.id.toString()
            })

    override fun apply(annotation: Setting?, field: Field?, pojo: Any?, builder: ConfigLeafBuilder<*, *>?) {
        builder!!.withAttribute(
            ConfigAttribute.create(
                FiberId("kami", "setting_interface"),
                INTERFACE_TYPE,
                typeMap.getOrDefault(
                    field!!.type,
                    typeMap.getOrDefault(builder.type.erasedPlatformType, SettingInterface.Default)
                )
            )
        )
    }

}