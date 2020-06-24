package me.zeroeightsix.kami.util;

import com.google.common.base.Converter;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by 086 on 13/12/2017.
 */
public class Friends {
    @Setting
    public static ArrayList<GameProfile> friends = new ArrayList<>();

    private Friends() {
    }

    public static boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public static void addFriend(GameProfile profile) {
        if (!isFriend(profile.getName())) {
            friends.add(profile);
        }
    }

    public static void removeFriend(GameProfile profile) {
        if (isFriend(profile.getName())) {
            friends.removeIf(profile1 -> profile1.equals(profile));
        }
    }

    public static class FriendListConverter extends Converter<ArrayList<GameProfile>, JsonElement> {
        public FriendListConverter() {}

        @Override
        protected JsonElement doForward(ArrayList<GameProfile> list) {
            JsonArray array = new JsonArray();
            for (GameProfile friend : list) {
                JsonObject object = new JsonObject();
                object.add("id", new JsonPrimitive(friend.getId().toString()));
                object.add("username", new JsonPrimitive(friend.getName()));
                array.add(object);
            }
            return array;
        }

        @Override
        protected ArrayList<GameProfile> doBackward(JsonElement jsonElement) {
            JsonArray v = jsonElement.getAsJsonArray();
            ArrayList<GameProfile> friends = new ArrayList<>();
            for (JsonElement element : v) {
                JsonObject object = element.getAsJsonObject();
                String username = object.get("username").getAsString();
                UUID uuid = UUID.fromString(object.get("id").getAsString());
                try {
                    friends.add(new GameProfile(uuid, getUsernameByUUID(uuid, username)));
                } catch (Exception ignored) {}
            }
            return friends;
        }

        private String getUsernameByUUID(UUID uuid, String saved) {
            String src = getSource("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
            if (src == null || src.isEmpty()) return saved;
            try {
                JsonElement object = new JsonParser().parse(src);
                return object.getAsJsonObject().get("name").getAsString();
            }catch (Exception e) {
                e.printStackTrace();
                System.err.println(src);
                return saved;
            }
        }

        private static String getSource(String link){
            try{
                URL u = new URL(link);
                URLConnection con = u.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    buffer.append(inputLine);
                in.close();

                return buffer.toString();
            }catch(Exception e){
                return null;
            }
        }
    }

}
