package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PlayerAttackBlockEvent;
import me.zeroeightsix.kami.feature.module.player.TpsSync;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 1))
    public void attackBlock(BlockPos position, Direction facing, CallbackInfoReturnable<Boolean> info) {
        PlayerAttackBlockEvent event = new PlayerAttackBlockEvent(position, facing);
        KamiMod.EVENT_BUS.post(event);
    }

    @Redirect(method = "method_2902", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView view, BlockPos pos) {
        return state.calcBlockBreakingDelta(player, view, pos) * (TpsSync.isSync() ? (LagCompensator.INSTANCE.getTickRate() / 20f) : 1);
    }

}
