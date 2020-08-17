package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import me.zeroeightsix.kami.feature.FindSettings

/**
 * Indicates that this class is capable of carrying a [ConfigBranch] of its own settings
 *
 * To be used in combination with [FindSettings]
 */
interface HasConfig {

    var config: ConfigBranch

}
