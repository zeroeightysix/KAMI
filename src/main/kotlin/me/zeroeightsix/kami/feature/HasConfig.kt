package me.zeroeightsix.kami.feature

import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch

/**
 * Indicates that this class is capable of carrying a [ConfigBranch] of its own settings
 *
 * To be used in combination with [FindSettings]
 */
interface HasConfig {

    var config: ConfigBranch
}
