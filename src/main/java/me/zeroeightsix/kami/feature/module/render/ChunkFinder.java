package me.zeroeightsix.kami.feature.module.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.ChunkEvent;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.SettingVisibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author 086 and IronException
 */
@Module.Info(name = "ChunkFinder", description = "Highlights newly generated chunks", category = Module.Category.RENDER)
public class ChunkFinder extends Module {

    @Setting
    private int yOffset = 0;
    @Setting
    private boolean relative = true;
    @Setting
    private boolean saveNewChunks = false;

    @Setting
    @SettingVisibility.Method("isSaveNewChunks")
    private SaveOption saveOption = SaveOption.extraFolder;

    @Setting
    @SettingVisibility.Method("isSaveNewChunks")
    private boolean saveInRegionFolder = false;

    @Setting
    @SettingVisibility.Method("isSaveNewChunks")
    private boolean alsoSaveNormalCoords = false;
    @EventHandler
    public Listener<ChunkEvent> listener = new Listener<>(event -> {
        if (!event.getPacket().isFullChunk()) {
            chunks.add(event.getChunk());
            dirty = true;
            if (saveNewChunks) {
                saveNewChunk(event.getChunk());
            }
        }
    });

    private LastSetting lastSetting = new LastSetting();
    private PrintWriter logWriter;

    static ArrayList<Chunk> chunks = new ArrayList<>();

    private static boolean dirty = true;
    private int list = -1;

    @EventHandler
    private Listener<RenderEvent.World> worldRenderListener = new Listener<>(event -> {
        if (dirty) {
            if (list == -1) {
                list = GL11.glGenLists(1);
                //list = GlStateManager.genLists(1);
            }
            GL11.glNewList(list, GL11.GL_COMPILE);

            GlStateManager.pushMatrix();
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.lineWidth(1.0f);
            for (Chunk chunk : chunks) {
                double x = chunk.getPos().x * 16;
                double y = 0;
                double z = chunk.getPos().z * 16;

                GlStateManager.color4f(.6f, .1f, .2f,1.0f);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
                bufferBuilder.begin(GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(x, y, z).color(.6f, .1f, .2f, 1f).next();
                bufferBuilder.vertex(x + 16, y, z).color(.6f, .1f, .2f, 1f).next();
                bufferBuilder.vertex(x + 16, y, z + 16).color(.6f, .1f, .2f, 1f).next();
                bufferBuilder.vertex(x, y, z + 16).color(.6f, .1f, .2f, 1f).next();
                bufferBuilder.vertex(x, y, z).color(.6f, .1f, .2f, 1f).next();
                tessellator.draw();
            }
            GlStateManager.disableBlend();
            GlStateManager.enableTexture();
            GlStateManager.enableDepthTest();
            GlStateManager.popMatrix();

            GL11.glEndList();
            dirty = false;
        }

        Camera camera = mc.getEntityRenderManager().camera;

        double x = camera.getPos().x;
        double y = (relative ? 1 : -1) * camera.getPos().y + yOffset;
        double z = camera.getPos().z;
        GlStateManager.translated(-x, y, -z);
        GL11.glCallList(list);
        GlStateManager.translated(x, -y, z);
    });

    @Override
    public void onDisable() {
        logWriterClose();
        chunks.clear();
    }

    public boolean isSaveNewChunks() {
        return saveNewChunks;
    }

    // needs to be synchronized so no data gets lost
    public void saveNewChunk(Chunk chunk) {
        saveNewChunk(testAndGetLogWriter(), getNewChunkInfo(chunk));
    }

    private String getNewChunkInfo(Chunk chunk) {
        String rV = String.format("%d,%d,%d", System.currentTimeMillis(), chunk.getPos().x, chunk.getPos().z);
        if (alsoSaveNormalCoords) {
            rV += String.format(",%d,%d", chunk.getPos().x * 16 + 8, chunk.getPos().z * 16 + 8);
        }
        return rV;
    }

    private PrintWriter testAndGetLogWriter() {
        if (lastSetting.testChangeAndUpdate()) {
            logWriterClose();
            logWriterOpen();
        }
        return logWriter;
    }

    private void logWriterOpen() {
        String filepath = getPath().toString();
        try {
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)), true);
            String head = "timestamp,ChunkX,ChunkZ";
            if (alsoSaveNormalCoords) {
                head += ",x coordinate,z coordinate";
            }
            logWriter.println(head);
        } catch (Exception e) {
            e.printStackTrace();
            KamiMod.getLog().error("some exception happened when trying to start the logging -> " + e.getMessage());
            Command.sendChatMessage("onLogStart: " + e.getMessage());
        }
    }

    private Path getPath() {
        // code from baritone (https://github.com/cabaletta/baritone/blob/master/src/main/java/baritone/cache/WorldProvider.java)
        File file = null;
        DimensionType dimension = mc.player.getEntityWorld().getDimension();

        // If there is an integrated server running (Aka Singleplayer) then do magic to find the world save file
        if (mc.isInSingleplayer()) {
            try {
                //file = mc.getServer().getWorld(dimension).getSaveHandler().getWorldDir();
                file = mc.getServer().getWorld(new RegistryKey<mc.world>()).getSaveHandler().getWorldDir();
            } catch (Exception e) {
                e.printStackTrace();
                KamiMod.getLog().error("some exception happened when getting canonicalFile -> " + e.getMessage());
                Command.sendChatMessage("onGetPath: " + e.getMessage());
            }

            // Gets the "depth" of this directory relative the the game's run directory, 2 is the location of the world
            if (file.toPath().relativize(mc.runDirectory.toPath()).getNameCount() != 2) {
                // subdirectory of the main save directory for this world

                file = file.getParentFile();
            }

        } else { // Otherwise, the server must be remote...
            file = makeMultiplayerDirectory().toFile();
        }

        // We will actually store the world data in a subfolder: "DIM<id>"
        if (dimension != DimensionType.getOverworldDimensionType()) { // except if it's the overworld
            file = new File(file, "DIM" + dimension);
        }

        // maybe we want to save it in region folder
        if (saveInRegionFolder) {
            file = new File(file, "region");
        }

        file = new File(file, "newChunkLogs");


        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        file = new File(file, mc.getSession().getUsername() + "_" + date + ".csv"); // maybe dont safe the name actually. But I also dont want to make another option...

        Path rV = file.toPath();
        try {
            if (!Files.exists(rV)) { // ovsly always...
                Files.createDirectories(rV.getParent());
                Files.createFile(rV);
            }
        } catch (IOException e) {
            e.printStackTrace();
            KamiMod.getLog().error("some exception happened when trying to make the file -> " + e.getMessage());
            Command.sendChatMessage("onCreateFile: " + e.getMessage());
        }
        return rV;
    }

    private Path makeMultiplayerDirectory() {
        File rV = MinecraftClient.getInstance().runDirectory;
        String folderName;
        switch (saveOption) {
            case liteLoaderWdl: // make folder structure like liteLoader
                folderName = mc.getCurrentServerEntry().name;

                rV = new File(rV, "saves");
                rV = new File(rV, folderName);
                break;
            case nhackWdl: // make folder structure like nhack-insdustries
                folderName = getNHackInetName();

                rV = new File(rV, "config");
                rV = new File(rV, "wdl-saves");
                rV = new File(rV, folderName);

                // extra because name might be different
                if (!rV.exists()) {
                    Command.sendChatMessage("nhack wdl directory doesnt exist: " + folderName);
                    Command.sendChatMessage("creating the directory now. It is recommended to update the ip");
                }
                break;
            default: // make folder structure in .minecraft
                folderName = mc.getCurrentServerEntry().name + "-" + mc.getCurrentServerEntry().address;
                if (SystemUtils.IS_OS_WINDOWS) {
                    folderName = folderName.replace(":", "_");
                }

                rV = new File(rV, "KAMI_NewChunks");
                rV = new File(rV, folderName);
        }

        return rV.toPath();
    }

    private String getNHackInetName() {
        String folderName = mc.getCurrentServerEntry().address;
        if (SystemUtils.IS_OS_WINDOWS) {
            folderName = folderName.replace(":", "_");
        }
        if (hasNoPort(folderName)) {
            folderName += "_25565"; // if there is no port then we have to manually include the standard port..
        }
        return folderName;
    }

    private boolean hasNoPort(String ip) {
        if (!ip.contains("_")) {
            return true;
        }

        String[] sp = ip.split("_");
        String ending = sp[sp.length - 1];
        if (!isInteger(ending)) { // if it is numeric it means it might be a port...
            return true;
        }
        return false;
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private void logWriterClose() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
    }

    private void saveNewChunk(PrintWriter log, String data) {
        log.println(data);
    }

//    @EventHandler
//    private Listener<net.minecraftforge.event.world.ChunkEvent.Unload> unloadListener = new Listener<>(event -> dirty = chunks.remove(event.getChunk()));

    private enum SaveOption {
        extraFolder, liteLoaderWdl, nhackWdl
    }

    private class LastSetting {

        SaveOption lastSaveOption;
        boolean lastInRegion;
        boolean lastSaveNormal;
        DimensionType dimension;
        String ip;

        public boolean testChangeAndUpdate() {
            if (testChange()) {
                // so we dont have to do this process again next time
                update();
                return true;
            }
            return false;
        }

        public boolean testChange() {
            // these somehow include the test wether its null
            if (saveOption != lastSaveOption) {
                return true;
            }
            if (saveInRegionFolder != lastInRegion) {
                return true;
            }
            if (alsoSaveNormalCoords != lastSaveNormal) {
                return true;
            }
            if (dimension != mc.player.getEntityWorld().getDimension()) {
                return true;
            }
            if (!mc.getCurrentServerEntry().address.equals(ip)) { // strings need equals + this way because could be null
                return true;
            }
            return false;
        }

        private void update() {
            lastSaveOption = saveOption;
            lastInRegion = saveInRegionFolder;
            lastSaveNormal = alsoSaveNormalCoords;
            dimension = mc.player.getEntityWorld().getDimension();
            ip = mc.getCurrentServerEntry().address;
        }
    }
}
