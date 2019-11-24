package me.zeroeightsix.kami.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlockRangeScanner {

    private BlockPos anchor;
    private final List<BlockPos> sphere = new ArrayList<>();
    private final double rangeSquared;

    public BlockRangeScanner(BlockPos anchor, double range) {
        this.anchor = anchor;
        this.rangeSquared = range * range;
        int bottom = (int) Math.ceil(-range);
        int size = (int) (range * 2);
        for (int x = bottom; x <= size; x++) {
            for (int y = bottom; y <= size; y++) {
                for (int z = bottom; z <= size; z++) {
                    if (x * x + y * y + z * z <= rangeSquared) {
                        sphere.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    public BlockRangeScanner(double range) {
        this(new BlockPos(0, 0, 0), range);
    }

    public List<BlockPos> updateAnchor(BlockPos newAnchor) {
        Vec3d diff = new Vec3d(newAnchor.x, newAnchor.y, newAnchor.z).subtract(anchor.x, anchor.y, anchor.z);

        // Anchor point hasn't been updated or hasn't moved a block
        if (diff.x == 0 && diff.y == 0 && diff.z == 0 || (anchor.x == newAnchor.x &&
                anchor.y == newAnchor.y &&
                anchor.z == newAnchor.z)) {
            return null;
        }

        List<BlockPos> change = new ArrayList<>();

        BlockPos diffPos = new BlockPos(diff);
        double diffLenSquared = diff.lengthSquared();
        for (BlockPos position : sphere) {
            position = position.add(diffPos);
            double dist = position.x * position.x + position.y * position.y + position.z * position.z;
            if (dist >= rangeSquared) {
                BlockPos pos = new BlockPos(anchor.add(position));
                change.add(pos);
            }
        }

        this.anchor = newAnchor;
        return change;
    }

    public BlockPos getAnchor() {
        return anchor;
    }
}
