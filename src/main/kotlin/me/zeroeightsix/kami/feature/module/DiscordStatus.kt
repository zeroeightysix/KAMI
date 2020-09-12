package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import net.arikia.dev.drpc.DiscordEventHandlers
import net.arikia.dev.drpc.DiscordRPC
import net.arikia.dev.drpc.DiscordRichPresence
import net.arikia.dev.drpc.DiscordUser
import net.minecraft.util.Util.OperatingSystem
import net.minecraft.util.Util.getOperatingSystem

@Module.Info(name = "DiscordRPC", category = Module.Category.MISC, description = "Discord Rich presence")
object DiscordStatus : Module() {
    @Setting(name = "Version")
    private var version = true

    @Setting(name = "Username")
    private var username = true

    @Setting(name = "Server")
    private var server = true

    @Setting(name = "Flair")
    private var message = true

    private const val updateLimit: Long = 15000L // 15 seconds, should be good. (prevents api rate limiting)
    private const val applicationId = "753664640789118999"
    private var lastUpdate: Long = 0L

    val messages = arrayOf(
            "kami red how???",
            "its really here",
            "bigrat.monster!!!",
            "free and open source ;)",
            "kami kami kami kami kami",
            "Scythe goddard mass gleaning",
            "i have a scoop!",
            "Not unfounded.",
            getOperatingSystemMessage(),
            "YES!!!!!!",
            "086 is NOT A PRIME NUMBER!",
            "3 is a PRIME NUMBER",
            "Also try our sister mod KAMI BLUE!",
            ":) (smiley face)",
            "Read rat ode by elizabeth acevedo"
    )

    override fun onEnable() {
        initDiscord()
    }

    override fun onDisable() {
        DiscordRPC.discordShutdown()
    }

    @EventHandler
    private val updateListener =
            Listener<TickEvent.Client.InGame>({
                if ((lastUpdate + updateLimit <= System.currentTimeMillis())) {
                    // If boolean is true return value else return ""
                    infix fun Boolean.to(string: String) = if (this) string else ""

                    // This is the bottom half of the RPC with server ip and funny message
                    val presence = DiscordRichPresence.Builder("${server to "127.0.0.1 (ez doxx)"}${(server && message) to " | "}${message to messages.random()}")
                            // This is the top half with modver and username
                            .setDetails("${username to "Notch"}${(username && version) to " | "}${version to KamiMod.MODVER}")
                            // Image key (kami) and text when you scroll over it (kamiclient.com)
                            .setBigImage("kami", "kamiclient.com")
                            // Small image key and text when hovered over
                            .setSmallImage("bigrat", "he is massive :)")

                    //Update the RPC
                    DiscordRPC.discordUpdatePresence(presence.build())
                    lastUpdate = System.currentTimeMillis()
                }
            })

    fun initDiscord() {
        val handlers = DiscordEventHandlers.Builder().build()
        DiscordRPC.discordInitialize(applicationId, handlers, false)
        DiscordRPC.discordRegister(applicationId, "")
        lastUpdate = System.currentTimeMillis() - updateLimit // Make it update instantly
    }

    private fun getOperatingSystemMessage() = when (getOperatingSystem()) {
        OperatingSystem.WINDOWS -> "win dow"
        OperatingSystem.OSX -> "appl e"
        OperatingSystem.LINUX -> "i use arch btw B)"
        else -> "wtf temple os how"
    }

}