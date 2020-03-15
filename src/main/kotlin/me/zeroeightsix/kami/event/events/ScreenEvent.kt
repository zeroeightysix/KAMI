package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.gui.screen.Screen

/**
 * Created by 086 on 17/11/2017.
 */
open class ScreenEvent(var screen: Screen?) : KamiEvent() {

    class Displayed(screen: Screen?) : ScreenEvent(screen)
    class Closed(screen: Screen?) : ScreenEvent(screen)

}