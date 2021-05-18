package me.zeroeightsix.kami.gui.wizard

import imgui.ImGui
import imgui.ImGui.dummy
import imgui.ImGui.openPopup
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.ImGui.textWrapped
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui.popItemFlag
import imgui.internal.ImGui.pushItemFlag
import imgui.internal.flag.ImGuiItemFlags
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.feature.module.Aura
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.popupModal
import me.zeroeightsix.kami.gui.ImguiDSL.radioButton
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.internalService

object Wizard {

    @Setting
    var firstTime = true

    private var monolithicMigrationNeeded =
        Paths.get("KAMI_config.json5").isRegularFile() && !Paths.get("kami", "core").isDirectory()

    init {
        KamiConfig.register(internalService("wizard"), this)
    }

    private var currentPage = 0

    private val setupPages = listOf(
        ::welcomePage,
        ::appearancePage,
        ::moduleWindowsPage,
        ::modifiersPage,
        ::widgetsPage,
        {
            firstTime = false
            currentPage = 0
        }
    )

    private val monoMigrationPages = listOf(
        ::monoMigrationPage,
        ::processMigrationPage,
        ::migrationSuccessPage,
        {
            monolithicMigrationNeeded = false
            currentPage = 0
        }
    )

    /**
     * Returns `true` if the wizard was opened
     */
    operator fun invoke(): Boolean {
        when {
            monolithicMigrationNeeded -> monoMigrationPages
            firstTime -> setupPages
            else -> return false
        }.let {
            showWizard(it)
        }

        return true
    }

    private fun showWizard(pages: List<() -> Unit>) {
        openPopup("KAMI wizard")
        setNextWindowPos(
            ImGui.getIO().displaySizeX * 0.5f,
            ImGui.getIO().displaySizeY * 0.5f,
            ImGuiCond.Always,
            0.5f,
            0.5f
        )
        popupModal(
            "KAMI wizard",
            extraFlags = ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoMove
        ) {
            pages[currentPage]()
            (currentPage == 0).conditionalWrap(
                {
                    pushItemFlag(ImGuiItemFlags.Disabled, true)
                    pushStyleVar(ImGuiStyleVar.Alpha, ImGui.getStyle().alpha * 0.5f)
                },
                {
                    button("Previous", 100f, 0f) {
                        currentPage--
                    }
                },
                {
                    popItemFlag()
                    popStyleVar()
                }
            )
            sameLine()
            button("Next", 100f, 0f) {
                currentPage++
            }
        }
    }

    // Setup pages

    private fun welcomePage() {
        text("Welcome to KAMI!")
        text("This wizard is going to take you through setting up the GUI to your liking.")
        dummy(10f, 10f)
        text("Everything set by the wizard can be manually changed later through the Settings menu.")
    }

    private fun appearancePage() {
        text("Please select your preferred theme and font.")
        Settings.showThemeSelector()
        Settings.showFontSelector()

        pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
        text("GUI is visible in the background")
        popStyleColor()

        // we take this opportunity to autosize the module windows
        Modules.resize = true
        KamiGuiScreen.renderGui() // Show the full GUI
    }

    private fun moduleWindowsPage() {
        text("How do you want your module windows to be set up?")

        radioButton("Per category", Modules.preferCategoryWindows) {
            Modules.preferCategoryWindows = true
            Modules.windows = Modules.getDefaultWindows()
        }
        radioButton("Everything in one window", !Modules.preferCategoryWindows) {
            Modules.preferCategoryWindows = false
            Modules.windows = Modules.getDefaultWindows()
        }

        pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
        textWrapped(
            "The module windows in KAMI are fully customizable. If neither choice appeals to you, you can manually reorganise the module windows through the module window editor."
        )
        textWrapped("The module window editor may be accessed through the `View` menu in the top menu bar.")
        popStyleColor()
    }

    private fun modifiersPage() {
        text("Should KAMI enable usage of modifier keys in binds?")
        text("Enabling this will make pressing e.g. 'Q' different from 'CTRL+Q'.")
        textWrapped("This has the sometimes unintended side effect of e.g. being unable to toggle a module while sneaking, if sneaking is bound to a modifier key.")
        checkbox("Enable modifier keys", Settings::modifiersEnabled)

        separator()

        pushStyleColor(ImGuiCol.Text, .7f, .7f, .7f, 1f)
        text("Assuming 'K' is bound to Aura,")
        text("And 'CTRL+Q' is bound to Brightness,")
        dummy(10f, 10f)
        val not = if (Settings.modifiersEnabled) " NOT " else " "
        text("Pressing K WILL toggle Aura.")
        text("Pressing SHIFT+K WILL${not}toggle Aura.")
        dummy(10f, 10f)
        text("Pressing CTRL+Q WILL toggle Brightness.")
        text("Pressing Q WILL${not}toggle Brightness.")
        popStyleColor()

        separator()
    }

    private fun widgetsPage() {
        text("How far from the edge should HUD elements be rendered?")
        Settings.showBorderOffsetSlider()
        separator()
        text("Which elements should be shown in the HUD?")
        EnabledWidgets.enabledButtons()
        separator()
        KamiGuiScreen.showWidgets(false)
    }

    // Monolithic config file migration page

    private fun monoMigrationPage() {
        textWrapped("An old KAMI configuration file is present, and the new configuration directory does not exist yet.")
        textWrapped("KAMI can automatically migrate this file to the new system for you. Would you like to proceed?")

        dummy(10f, 10f)

        checkbox("Remove old file", MonoMigrationOptions::removeOldFile)
        button("Don't migrate") {
            monolithicMigrationNeeded = false
        }
    }

    private fun processMigrationPage() {
        if (MonoMigrationOptions.error == null) {
            try {
                KamiConfig.loadFromMonolithicConfig()
                KamiConfig.saveAll()
            } catch (e: Exception) {
                MonoMigrationOptions.error = e.stackTraceToString()
                return
            }

            if (MonoMigrationOptions.removeOldFile) {
                Paths.get("KAMI_config.json5").deleteIfExists()
            }

            currentPage++
            return
        }

        textWrapped("An error occured while migrating:")
        textDisabled(MonoMigrationOptions.error)
    }

    private fun migrationSuccessPage() {
        if (MonoMigrationOptions.error != null) {
            currentPage++
            return
        }

        text("Migration successful!")
        text("That didn't take very long, did it?")
        text("Just press next to start enjoying KAMI again.")
    }

    private object MonoMigrationOptions {
        var removeOldFile = true
        var error: String? = null
    }
}