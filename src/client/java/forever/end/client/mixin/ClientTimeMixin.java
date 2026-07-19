package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Time Changer: \u043a\u043e\u0433\u0434\u0430 \u043c\u043e\u0434\u0443\u043b\u044c \u0432\u043a\u043b\u044e\u0447\u0451\u043d, \u0433\u043b\u0443\u0448\u0438\u043c \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u044b\u0439 \u043f\u0430\u043a\u0435\u0442 \u0432\u0440\u0435\u043c\u0435\u043d\u0438,
 * \u0447\u0442\u043e\u0431\u044b \u043a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u043d\u0435 \u0441\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u043b\u043e\u0441\u044c \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0435 \u0440\u0430\u0437 \u0432 \u0441\u0435\u043a\u0443\u043d\u0434\u0443.
 */
@Mixin(ClientPacketListener.class)
public class ClientTimeMixin {
    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true, require = 0)
    private void ecf$blockTime(CallbackInfo ci) {
        Module m = Modules.find("Time Changer");
        if (m != null && m.enabled) ci.cancel();
    }
}
