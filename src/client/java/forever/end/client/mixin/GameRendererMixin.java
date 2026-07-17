package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import forever.end.client.ecf.fx.CameraFx;
import net.minecraft.client.renderer.GameRenderer;

/**
 * Модификация итогового FOV для модулей Zoom и FOV Changer.
 * Позволяет зуму/обзору выходить за ванильный предел 30–110 и работать плавно покадрово.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void ecf$modifyFov(CallbackInfoReturnable<Float> cir) {
        float base = cir.getReturnValueF();
        float out = CameraFx.applyFov(base);
        if (out != base) cir.setReturnValue(out);
    }
}
