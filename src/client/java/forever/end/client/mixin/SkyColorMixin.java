package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import forever.end.client.ecf.fx.WorldFx;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Custom Sky: \u043f\u043e\u0434\u043c\u0435\u0448\u0438\u0432\u0430\u0435\u043c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u0446\u0432\u0435\u0442 \u0432 \u043d\u0430\u0441\u0442\u043e\u044f\u0449\u0438\u0439 \u0446\u0432\u0435\u0442 \u043d\u0435\u0431\u0430 \u0434\u0432\u0438\u0436\u043a\u0430.
 * getSkyColor \u0432 1.21.4 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u0442 int (ARGB). \u041a\u0440\u0430\u0441\u0438\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u043d\u0435\u0431\u043e/\u0442\u0443\u043c\u0430\u043d, \u043d\u0435 \u0432\u0435\u0441\u044c \u044d\u043a\u0440\u0430\u043d.
 */
@Mixin(ClientLevel.class)
public class SkyColorMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void ecf$customSky(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(WorldFx.applySkyColor(cir.getReturnValue()));
    }
}
