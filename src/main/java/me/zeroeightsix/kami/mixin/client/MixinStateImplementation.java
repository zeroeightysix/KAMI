package me.zeroeightsix.kami.mixin.client;

//@Mixin(BlockStateContainer.StateImplementation.class)
public class MixinStateImplementation {

    /*@Shadow @Final private Block block;

    @Redirect(method = "addCollisionBoxToList", at = @At(value="INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"))
    public void addCollisionBoxToList(Block b, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        AddCollisionBoxToListEvent event = new AddCollisionBoxToListEvent(b, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        KamiMod.EVENT_BUS.post(event);
        if (!event.isCancelled())
            block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }*/ //TODO

}
