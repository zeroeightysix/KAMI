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
class DiscordStatus : Module() {
    @Setting(name = "Version")
    private var version = true

    @Setting(name = "Username")
    private var username = true

    @Setting(name = "Server")
    private var server = true

    @Setting(name = "Flair")
    private var message = true

    var lastUpdate: Long = 0L
    var rpcEnabled = false

    val messages = arrayOf(
            "kami red how???",
            "its really here",
            "bigrat.monster!!!",
            "free and open source ;)",
            "kami kami kami kami kami",
            "Scythe goddard mass gleaning",
            "i have a scoop!",
            "Not unfounded.",
            "Rockin' version " + KamiMod.MODVER,
            getOperatingSystemMessage(),
            "YES!!!!!!",
            "086 is NOT A PRIME NUMBER!",
            "3 is a PRIME NUMBER",
            "Also try our sister mod KAMI BLUE!"
    )

    override fun onEnable() {
        rpcEnabled = true
        initDiscord()
    }

    override fun onDisable() {
        DiscordRPC.discordShutdown()
        rpcEnabled = false
        println("downdoot")
    }

    @EventHandler
    private val updateListener =
            Listener<TickEvent.Client.InGame>({
                if (lastUpdate == 0L) lastUpdate = System.currentTimeMillis()
                if ((lastUpdate + 15000L <= System.currentTimeMillis()) && rpcEnabled) { // 15 seconds, should be good.
                    DiscordRPC.discordRunCallbacks()
                    var bottomString = ""
                    var topString = ""
                    if (message && server) bottomString = ("127.0.0.1 (ez doxx)") + " | " + messages.random()
                    else if (message) bottomString = messages.random()
                    else if (server) bottomString = ("127.0.0.1 (ez doxx)")

                    if (version && username) topString = (KamiMod.MODVER) + " | Notch"
                    else if (version) topString = messages.random()
                    else if (username) topString = ("Notch")


                    val presence = DiscordRichPresence.Builder(bottomString) // This is the bottom half of the RPC with server ip and funny message
                    presence.setDetails(topString) // This is the top half with modver and username
                    presence.setBigImage("kami", "kamiclient.com") // Image key (kami) and text when you scroll over it (kamiclient.com)
                    DiscordRPC.discordUpdatePresence(presence.build())
                    lastUpdate += 15000L
                }


            })

    // Yes, its the same code but again. Yes, I know its bad practice but at the moment I don't care.
    fun initDiscord() {
        var bottomString = ""
        var topString = ""
        if (message && server)  bottomString = ("127.0.0.1 (ez doxx)") + " | " + messages.random()
        else if (message) bottomString = messages.random()
        else if (server) bottomString = ("127.0.0.1 (ez doxx)")

        if (version && username) topString =  (KamiMod.MODVER) + " | Notch"
        else if (version) topString = messages.random()
        else if (username) topString = ("Notch")

        val handlers = DiscordEventHandlers.Builder().setReadyEventHandler { user: DiscordUser ->
            val presence = DiscordRichPresence.Builder(bottomString)
            presence.setDetails(topString)
            DiscordRPC.discordUpdatePresence(presence.build())
        }.build()

        DiscordRPC.discordInitialize("753664640789118999", handlers, false);
        DiscordRPC.discordRegister("753664640789118999", "");
        DiscordRPC.discordRunCallbacks()
        println("lol")
    }

    fun getOperatingSystemMessage() = when (getOperatingSystem()) {
        OperatingSystem.WINDOWS -> "win dow"
        OperatingSystem.OSX -> "appl e"
        OperatingSystem.LINUX -> "i use arch btw B)"
        else -> "wtf temple os how"
    }

}
