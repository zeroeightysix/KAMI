package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.ColourUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Created by 086 on 20/05/2018.
 */
// @Module.Info(name = "Carpenter", category = Module.Category.MISC)
public class Carpenter extends Module {

    private int displayList = -1;

    public static class ShapeBuilder {
        private static BlockPos from(double x, double y, double z) {
            return new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
        }

        public static Shape oval(BlockPos origin, double width, double length) {
            return null;
        }
    }

    public static class Selection {
        private BlockPos first;
        private BlockPos second;

        public Selection(BlockPos pos1, BlockPos pos2) {
            this.first = pos1;
            this.second = pos2;
        }

        public BlockPos getFirst() {
            return first;
        }

        public BlockPos getSecond() {
            return second;
        }

        public void setFirst(BlockPos first) {
            this.first = first;
        }

        public void setSecond(BlockPos second) {
            this.second = second;
        }

        public boolean isInvalid(){
            return first == null || second == null;
        }

        public int getWidth(){
            int x1 = Math.min(first.getX(), second.getX());
            int x2 = Math.max(first.getX(), second.getX())+1;
            return Math.abs(x1 - x2);
        }

        public int getLength(){
            int z1 = Math.min(first.getZ(), second.getZ());
            int z2 = Math.max(first.getZ(), second.getZ())+1;
            return Math.abs(z1 - z2);
        }

        public int getHeight(){
            int y1 = Math.min(first.getY(), second.getY())+1;
            int y2 = Math.max(first.getY(), second.getY());
            return Math.abs(y1 - y2);
        }

        public int getSize(){
            return getWidth() * getLength() * getHeight();
        }

        public BlockPos getMinimum(){
            int x1 = Math.min(first.getX(), second.getX());
            int y1 = Math.min(first.getY(), second.getY());
            int z1 = Math.min(first.getZ(), second.getZ());
            return new BlockPos(x1, y1, z1);
        }

        public BlockPos getMaximum(){
            int x1 = Math.min(first.getX(), second.getX())+1;
            int y1 = Math.min(first.getY(), second.getY());
            int z1 = Math.min(first.getZ(), second.getZ())+1;
            return new BlockPos(x1, y1, z1);
        }

        public BlockPos getFurthest(int x, int y, int z){
            if (x > 0){
                if (first.getX() > second.getX())
                    return first;
                else
                    return second;
            }else if (x < 0){
                if (first.getX() < second.getX())
                    return first;
                else
                    return second;
            }else if (y > 0){
                if (first.getX() > second.getX())
                    return first;
                else
                    return second;
            }else if (y < 0){
                if (first.getY() < second.getY())
                    return first;
                else
                    return second;
            }else if (z > 0){
                if (first.getZ() > second.getZ())
                    return first;
                else
                    return second;
            }else if (z < 0){
                if (first.getZ() < second.getZ())
                    return first;
                else
                    return second;
            }
            return null;
        }

        public void moveSelection(int x, int y, int z){
            first = first.add(x, y, z);
            second = second.add(x, y, z);
        }

        public void expand(int amount, EnumFacing facing){
            BlockPos affect = second;
            switch (facing){
                case DOWN:
                    affect = second.getY() < first.getY() ? second = (second.add(0, -amount, 0)) : (first = first.add(0, -amount, 0));
                    break;
                case UP:
                    affect = second.getY() > first.getY() ? second = (second.add(0, amount, 0)) : (first = first.add(0, amount, 0));
                    break;
                case NORTH:
                    affect = second.getZ() < first.getZ() ? second = (second.add(0, 0, -amount)) : (first = first.add(0, 0, -amount));
                    break;
                case SOUTH:
                    affect = second.getZ() > first.getZ() ? second = (second.add(0, 0, amount)) : (first = first.add(0, 0, amount));
                    break;
                case WEST:
                    affect = second.getX() < first.getX() ? second = (second.add(-amount, 0, 0)) : (first = first.add(-amount, 0, 0));
                    break;
                case EAST:
                    affect = second.getX() > first.getX() ? second = (second.add(amount, 0, 0)) : (first = first.add(amount, 0, 0));
                    break;
            }
        }
    }

    public class Shape {
        final BlockPos[] blocks;
        private final int colour;

        Shape (List<BlockPos> blocks) {
            this.blocks = blocks.toArray(new BlockPos[0]);
            this.colour = ColourUtils.toRGBA(.5f+Math.random()*.5f,.5f+Math.random()*.5f,.5f+Math.random()*.5f,1);
        }

        public BlockPos[] getBlocks() {
            return blocks;
        }

        public int getColour() {
            return colour;
        }
    }

}
