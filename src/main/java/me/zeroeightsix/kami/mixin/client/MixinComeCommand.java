package me.zeroeightsix.kami.mixin.client;

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.pathing.goals.GoalBlock;
import baritone.command.defaults.ComeCommand;
import me.zeroeightsix.kami.feature.module.Freecam;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// If baritone is not present at runtime, this will just produce a harmless error in console.
@Mixin(value = ComeCommand.class, remap = false)
public abstract class MixinComeCommand extends Command {

    protected MixinComeCommand(IBaritone baritone, String... names) {
        super(baritone, names);
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lbaritone/api/process/ICustomGoalProcess;setGoalAndPath(Lbaritone/api/pathing/goals/Goal;)V"), cancellable = true)
    public void onSetGoalAndPath(String label, IArgConsumer args, CallbackInfo ci) {
        if (Freecam.INSTANCE.getEnabled()) {
            ci.cancel();
            Vec3d pos = Freecam.INSTANCE.getPos();
            baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock((int) pos.x, (int) pos.y, (int) pos.z));
            logDirect("Coming (to KAMI freecam)");
        }
    }

}
