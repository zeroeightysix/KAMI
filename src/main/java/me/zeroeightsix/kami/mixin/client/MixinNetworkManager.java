package me.zeroeightsix.kami.mixin.client;

/**
 * Created by 086 on 13/11/2017.
 */
//@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    /*@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
        if (p_exceptionCaught_2_ instanceof IOException && NoPacketKick.isEnabled()) info.cancel();
    }*/ //TODO

}
