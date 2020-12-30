package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.SignSpammer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(
            method = "onSignEditorOpen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;openEditSignScreen(Lnet/minecraft/block/entity/SignBlockEntity;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onSignEditorOpen(SignEditorOpenS2CPacket packet, CallbackInfo ci, BlockEntity blockEntity) {
        // this variable is just here for some deduplication
        SignSpammer spammer = SignSpammer.INSTANCE;
        if (spammer.getEnabled()) {
            this.sendPacket(
                    new UpdateSignC2SPacket(
                            packet.getPos(),
                            spammer.getLine1(),
                            spammer.getLine2(),
                            spammer.getLine3(),
                            spammer.getLine4()
                    )
            );
            blockEntity.markDirty();
            // cancel to not open edit screen
            ci.cancel();
        }
    }
}
