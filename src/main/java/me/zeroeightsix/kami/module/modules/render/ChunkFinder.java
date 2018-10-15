package me.zeroeightsix.kami.module.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.ChunkEvent;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author 086
 */
@Module.Info(name = "ChunkFinder", description = "Highlights newly generated chunks", category = Module.Category.RENDER)
public class ChunkFinder extends Module {

    private Setting<Integer> yOffset = register(Settings.i("Y Offset", 0));
    private Setting<Boolean> relative = register(Settings.b("Relative", true));

    static ArrayList<Chunk> chunks = new ArrayList<>();

    private static boolean dirty = true;
    private int list = GL11.glGenLists(1);

    @Override
    public void onWorldRender(RenderEvent event) {
        if (dirty) {
            GL11.glNewList(list, GL11.GL_COMPILE);

            glPushMatrix();
            glEnable(GL_LINE_SMOOTH);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_TEXTURE_2D);
            glDepthMask(false);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_BLEND);
            glLineWidth(1.0F);
            for (Chunk chunk : chunks) {
                double posX = chunk.x * 16;
                double posY = 0;
                double posZ = chunk.z * 16;

                glColor3f(.6f, .1f, .2f);

                glBegin(GL_LINE_LOOP);
                glVertex3d(posX, posY, posZ);
                glVertex3d(posX + 16, posY, posZ);
                glVertex3d(posX + 16, posY, posZ + 16);
                glVertex3d(posX, posY, posZ + 16);
                glVertex3d(posX, posY, posZ);
                glEnd();
            }
            glDisable(GL_BLEND);
            glDepthMask(true);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glPopMatrix();
            glColor4f(1, 1, 1, 1);

            GL11.glEndList();
            dirty = false;
        }

        double x = mc.getRenderManager().renderPosX;
        double y = relative.getValue() ? 0 : -mc.getRenderManager().renderPosY;
        double z = mc.getRenderManager().renderPosZ;
        GL11.glTranslated(-x, y + yOffset.getValue(), -z);
        GL11.glCallList(list);
        GL11.glTranslated(x, -(y + yOffset.getValue()), z);
    }

    @EventHandler
    public Listener<ChunkEvent> listener = new Listener<>(event -> {
        if (!event.getPacket().isFullChunk()) {
            chunks.add(event.getChunk());
            dirty = true;
        }
    });

    @EventHandler
    private Listener<net.minecraftforge.event.world.ChunkEvent.Unload> unloadListener = new Listener<>(event -> dirty = chunks.remove(event.getChunk()));

    @Override
    public void destroy() {
        GL11.glDeleteLists(1, 1);
    }
}
