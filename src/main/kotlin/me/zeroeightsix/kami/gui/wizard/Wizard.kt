package me.zeroeightsix.kami.gui.wizard

import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.dummy
import imgui.ImGui.io
import imgui.dsl.popupModal
import imgui.ImGui.text
import imgui.ImGui.openPopup
import imgui.ImGui.popItemFlag
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushItemFlag
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowPos
import imgui.dsl.button
import imgui.internal.ItemFlag
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.conditionalWrap

object Wizard {
    
    @Setting
    var firstTime = true
    
    val pages = listOf({
        text("Welcome to KAMI!")
        text("This wizard is going to take you through setting up the GUI to your liking.")
        dummy(Vec2(10))
        text("Everything set by the wizard can be manually changed later through the Settings menu.")
    }, {

    }, {
        firstTime = false
    })
    
    var currentPage = 0

    /**
     * Returns `true` if the wizard was opened
     */
    operator fun invoke(): Boolean {
        if (firstTime) {
            openPopup("Setup wizard")
            setNextWindowPos(Vec2(io.displaySize.x * 0.5f, io.displaySize.y * 0.5f), Cond.Always, Vec2(0.5f))
            popupModal("Setup wizard", extraFlags = WindowFlag.AlwaysAutoResize or WindowFlag.NoTitleBar or WindowFlag.NoMove.i) {
                pages[currentPage]()
                (currentPage == 0).conditionalWrap(
                    {
                        pushItemFlag(ItemFlag.Disabled.i, true)
                        pushStyleVar(StyleVar.Alpha, ImGui.style.alpha * 0.5f)
                    },
                    {
                        button("Previous", Vec2(100, 0)) {
                            currentPage--
                        }
                    },
                    {
                        popItemFlag()
                        popStyleVar()
                    }
                )
                sameLine()
                button("Next", Vec2(100, 0)) {
                    currentPage++
                }
            }
        }

        return firstTime
    }
    
}