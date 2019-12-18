package me.zeroeightsix.kami.command.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import me.zeroeightsix.kami.mixin.client.IEntitySelector;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static me.zeroeightsix.kami.util.Texts.*;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.YELLOW;

/**
 * Created by 086 on 14/12/2017.
 */
public class FriendCommand extends Command {

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralText(o.toString()));

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                (LiteralArgumentBuilder<CommandSource>) LiteralArgumentBuilder.<CommandSource>literal("friend")
                        .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                KamiCommandSource source = (KamiCommandSource) context.getSource();
                                if (Friends.friends.getValue().isEmpty()) {
                                    source.sendFeedback(flit(GOLD, "You don't have any friends."));
                                    return 0;
                                }

                                final Text[] text = { null };
                                Friends.friends.getValue().stream().map(GameProfile::getName).forEach(s -> {
                                    if (text[0] == null) {
                                        text[0] = flit(YELLOW, s);
                                    } else {
                                        text[0] = text[0].append(append(lit(", "), flit(YELLOW, s)));
                                    }
                                });
                                text[0] = f(GOLD, text[0]);
                                source.sendFeedback(i(flit(GOLD, "You have the following friends:")));
                                source.sendFeedback(text[0]);
                                return 0;
                            })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                            .then(
                                    createFriendArgument(entry -> {
                                        if (Friends.isFriend(entry.getProfile().getName())) {
                                            return FAILED_EXCEPTION.create("That player is already your friend!");
                                        }
                                        return null;
                                    }, (entry, source) -> {
                                        Friends.addFriend(entry.getProfile());
                                        source.sendFeedback(
                                                f(GOLD, append(
                                                        lit("Added "),
                                                        flit(YELLOW, entry.getProfile().getName()),
                                                        lit(" to your friends list!")
                                                )));
                                        return 0;
                                    })
                            )
                        )
                        .then(LiteralArgumentBuilder.literal("remove")
                                .then(
                                        createFriendArgument(entry -> {
                                            if (!Friends.isFriend(entry.getProfile().getName())) {
                                                return FAILED_EXCEPTION.create("That player isn't your friend!");
                                            }
                                            return null;
                                        }, (entry, source) -> {
                                            Friends.removeFriend(entry.getProfile());
                                            source.sendFeedback(
                                                    f(GOLD, append(
                                                            lit("Removed "),
                                                            flit(YELLOW, entry.getProfile().getName()),
                                                            lit(" from your friends list!")
                                                    ))
                                            );
                                            return 0;
                                        })
                                )
                        )
        );
    }

    private static RequiredArgumentBuilder createFriendArgument(Function<PlayerListEntry, CommandSyntaxException> fail, BiFunction<PlayerListEntry, KamiCommandSource, Integer> function) {
        return RequiredArgumentBuilder.<CommandSource, EntitySelector>argument("friend", EntityArgumentType.player())
                .executes(context -> {
                    EntitySelector selector = context.getArgument("friend", EntitySelector.class);
                    Optional<PlayerListEntry> optionalPlayer = Wrapper.getMinecraft().getNetworkHandler().getPlayerList().stream().filter(playerListEntry -> {
                        String playerName = ((IEntitySelector) selector).getPlayerName();
                        if (playerName != null) {
                            return playerListEntry.getProfile().getName().equalsIgnoreCase(playerName);
                        } else {
                            return playerListEntry.getProfile().getId().equals(((IEntitySelector) selector).getUuid());
                        }
                    }).findAny();
                    if (optionalPlayer.isPresent()) {
                        PlayerListEntry entry = optionalPlayer.get();
                        CommandSyntaxException e = fail.apply(entry);
                        if (e != null) {
                            throw e;
                        }
                        return function.apply(entry, (KamiCommandSource) context.getSource());
                    } else {
                        // TODO: Process offline players
                        throw FAILED_EXCEPTION.create("Couldn't find that player.");
                    }
                });
    }

    private static String requestIDs(String data) {
        try{
            String query = "https://api.mojang.com/profiles/minecraft";
            String json = data;

            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String res = convertStreamToString(in);
            in.close();
            conn.disconnect();

            return res;
        }catch (Exception e) {
            return null;
        }
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String r = s.hasNext() ? s.next() : "/";
        return r;
    }
}
