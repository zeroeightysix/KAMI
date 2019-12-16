package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.Vec3d;

/**
 * Created by 086 on 10/12/2017.
 * https://github.com/fr1kin/ForgeHax/blob/4697e629f7fa4f85faa66f9ac080573407a6d078/src/main/java/com/matt/forgehax/events/RenderEvent.java
 */
public class RenderEvent extends KamiEvent {

    private final Stage stage;

    private RenderEvent(Stage stage) {
        this.stage = stage;
    }

    public enum Stage {
        WORLD, SCREEN
    }

    public static class Screen extends RenderEvent {
        public Screen() {
            super(Stage.SCREEN);
        }
    }

    public static class World extends RenderEvent {

        private final Tessellator tessellator;
        private final Vec3d renderPos;

        public World(Tessellator tessellator, Vec3d renderPos) {
            super(Stage.WORLD);
            this.tessellator = tessellator;
            this.renderPos = renderPos;
            setEra(Era.POST);
        }

        public Tessellator getTessellator() {
            return tessellator;
        }

        public BufferBuilder getBuffer() {
            return tessellator.getBufferBuilder();
        }

        public Vec3d getRenderPos() {
            return renderPos;
        }

        public void setTranslation(Vec3d translation) {
            getBuffer().setOffset(-translation.x, -translation.y, -translation.z);
        }

        public void resetTranslation() {
            setTranslation(renderPos);
        }

    }

}
