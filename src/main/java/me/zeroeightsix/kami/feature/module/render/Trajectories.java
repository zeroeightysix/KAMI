package me.zeroeightsix.kami.feature.module.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

/**
 * Created by 086 on 28/12/2017.
 */
@Module.Info(name = "Trajectories", category = Module.Category.RENDER)
public class Trajectories extends Module {
    ArrayList<Vec3d> positions = new ArrayList<>();
    HueCycler cycler = new HueCycler(100);
    
    @EventHandler
    public Listener<RenderEvent.World> worldListener = new Listener<>(event -> {
        try {
            StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                    .filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> (LivingEntity) entity)
                    .forEach(entity -> {
                positions.clear();
                TrajectoryCalculator.ThrowingType tt = TrajectoryCalculator.getThrowType(entity);
                if (tt == TrajectoryCalculator.ThrowingType.NONE) return;
                TrajectoryCalculator.FlightPath flightPath = new TrajectoryCalculator.FlightPath(entity, tt);

                while (!flightPath.isCollided()) {
                    flightPath.onUpdate();
                    positions.add(flightPath.position);
                }

                BlockPos hit = null;
                if (flightPath.getCollidingTarget() != null) {
                    hit = new BlockPos(flightPath.getCollidingTarget().getPos());
                }

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                if (hit != null){
                    KamiTessellator.prepare(GL11.GL_QUADS);
                    GL11.glColor4f(1,1,1,.3f);
                    if (flightPath.getCollidingTarget() instanceof BlockHitResult) {
                        KamiTessellator.drawBox(hit, 0x33ffffff, GeometryMasks.FACEMAP.get(((BlockHitResult) flightPath.getCollidingTarget()).getSide()));
                    }
                    KamiTessellator.release();
                }

                if (positions.isEmpty()) return;
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);

                GL11.glLineWidth(2F);
                if (hit != null)
                    GL11.glColor3f(1f, 1f, 1f);
                else
                    cycler.setNext();
                GL11.glBegin(GL11.GL_LINES);

                Vec3d a = positions.get(0);
                Vec3d renderPos = Wrapper.getRenderPosition();
                GL11.glVertex3d(a.x - renderPos.x, a.y - renderPos.y, a.z - renderPos.z);
                for (Vec3d v : positions) {
                    GL11.glVertex3d(v.x - renderPos.x, v.y - renderPos.y, v.z - renderPos.z);
                    GL11.glVertex3d(v.x - renderPos.x, v.y - renderPos.y, v.z - renderPos.z);
                    if (hit == null)
                        cycler.setNext();
                }

                GL11.glEnd();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                cycler.reset();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

}
