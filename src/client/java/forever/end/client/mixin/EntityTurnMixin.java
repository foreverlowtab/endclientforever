package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import forever.end.client.ecf.fx.CameraFx;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Freelook: пока активен свободный обзор, движение мыши игрока уходит в камеру,
 * а не поворачивает самого игрока.
 */
@Mixin(Entity.class)
public class EntityTurnMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void ecf$freelookTurn(double yRot, double xRot, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (CameraFx.freelookActive() && self == Minecraft.getInstance().player) {
            CameraFx.addFreeLook(yRot, xRot);
            ci.cancel();
        }
    }
}
