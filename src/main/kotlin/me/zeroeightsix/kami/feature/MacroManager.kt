package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.feature.command.Command
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.server.network.packet.ChatMessageC2SPacket

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/05/20
 */
object MacroManager {

    /**
     * Reads macros from KAMIMacros.json into the macros Map
     */
    fun registerMacros() {
        KamiMod.log.info("Registering macros...")
        Macro.readFileToMemory()
        KamiMod.log.info("Macros registered")
    }

    /**
     * Saves macros from the macros Map into KAMIMacros.json
     */
    fun saveMacros() {
        KamiMod.log.info("Saving macros...")
        Macro.writeMemoryToFile()
        KamiMod.log.info("Macros saved")
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keycode Int keycode of the key the was pressed
     */
    fun sendMacro(keycode: Int) {
        val macrosForThisKey = Macro.getMacrosForKey(keycode) ?: return
        for (currentMacro in macrosForThisKey) {
            if (currentMacro!!.startsWith(Command.getCommandPrefix())) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
//                MessageSendHelper.sendKamiCommand(currentMacro, false) // ie, the 'false' here
                // TODO: I can't figure out how
            } else {
                sendServerMessage(currentMacro)
            }
        }
    }

    /**
     * Helper for sending a literal chat packet. You might want to convert this to a util or something you can reuse.
     */
    private fun sendServerMessage(message: String) {
        if (Wrapper.getMinecraft().player != null) {
            Wrapper.getPlayer().networkHandler.sendPacket(ChatMessageC2SPacket(message))
        } else {
            KamiMod.log.warn("Could not send server message: \"$message\"")
        }
    }
}