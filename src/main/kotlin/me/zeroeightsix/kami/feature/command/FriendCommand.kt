package me.zeroeightsix.kami.feature.command

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.mixin.client.IEntitySelector
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.ITALIC
import net.minecraft.util.Formatting.YELLOW
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import java.util.function.BiFunction
import java.util.function.Function

object FriendCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("friend") {
            literal("list") {
                does {
                    val source = it.source as KamiCommandSource

                    if (Friends.friends.isEmpty()) {
                        it replyWith text(GOLD, "You don't have any friends.")
                        return@does 0
                    }

                    var text: MutableText? = null
                    Friends.friends
                        .mapNotNull { obj: GameProfile -> obj.name }
                        .forEach { s: String ->
                            if (text == null) {
                                text = text(YELLOW, s)
                            } else {
                                text?.append(
                                    text {
                                        +", "
                                        +s(YELLOW)
                                    }
                                )
                            }
                        }

                    source replyWith text(ITALIC) {
                        +"You have the following friends:"(GOLD)
                    }
                    text?.formatted(GOLD)?.let { text ->
                        source replyWith text
                    }
                    0
                }
            }
            literal("add") {
                then(
                    createFriendArgument(
                        Function { entry: PlayerListEntry ->
                            if (Friends.isFriend(entry.profile.name)) {
                                return@Function FAILED_EXCEPTION.create(
                                    "That player is already your friend!"
                                )
                            }
                            null
                        },
                        { entry: PlayerListEntry, source: KamiCommandSource ->
                            Friends.addFriend(entry.profile)
                            source replyWith text(GOLD) {
                                +"Added "
                                +entry.profile.name(YELLOW)
                                +" to your friends list!"
                            }
                            0
                        }
                    )
                )
            }
            literal("remove") {
                then(
                    createFriendArgument(
                        Function { entry: PlayerListEntry ->
                            if (!Friends.isFriend(entry.profile.name)) {
                                return@Function FAILED_EXCEPTION.create(
                                    "That player isn't your friend!"
                                )
                            }
                            null
                        },
                        { entry: PlayerListEntry, source: KamiCommandSource ->
                            Friends.removeFriend(entry.profile)
                            source replyWith text(GOLD) {
                                +"Removed "
                                +entry.profile.name(YELLOW)
                                +" from your friends list!"
                            }
                            0
                        }
                    )
                )
            }
        }
    }

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType { o: Any ->
            LiteralText(o.toString())
        }

    private fun createFriendArgument(
        fail: Function<PlayerListEntry, CommandSyntaxException?>,
        function: BiFunction<PlayerListEntry, KamiCommandSource, Int>
    ): RequiredArgumentBuilder<CommandSource, EntitySelector> {
        return RequiredArgumentBuilder.argument<CommandSource, EntitySelector>(
            "friend",
            EntityArgumentType.player()
        )
            .executes { ctx ->
                val selector: EntitySelector = "friend" from ctx
                val optionalPlayer =
                    Wrapper.getMinecraft().networkHandler!!.playerList.stream()
                        .filter { playerListEntry: PlayerListEntry ->
                            val playerName = (selector as IEntitySelector).playerName
                            if (playerName != null) {
                                return@filter playerListEntry.profile.name
                                    .equals(playerName, ignoreCase = true)
                            } else {
                                return@filter playerListEntry.profile.id == (selector as IEntitySelector).uuid
                            }
                        }.findAny()
                if (optionalPlayer.isPresent) {
                    val entry = optionalPlayer.get()
                    val e = fail.apply(entry)
                    if (e != null) {
                        throw e
                    }
                    return@executes function.apply(entry, ctx.source as KamiCommandSource)
                } else { // TODO: Process offline players
                    throw FAILED_EXCEPTION.create("Couldn't find that player.")
                }
            }
    }

    private fun requestIDs(data: String): String? {
        return try {
            val query = "https://api.mojang.com/profiles/minecraft"
            val url = URL(query)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.doInput = true
            conn.requestMethod = "POST"
            val os = conn.outputStream
            os.write(data.toByteArray(charset("UTF-8")))
            os.close()
            // read the response
            val input: InputStream = BufferedInputStream(conn.inputStream)
            val res =
                convertStreamToString(
                    input
                )
            input.close()
            conn.disconnect()
            res
        } catch (e: Exception) {
            null
        }
    }

    private fun convertStreamToString(stream: InputStream): String {
        val s = Scanner(stream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else "/"
    }
}
