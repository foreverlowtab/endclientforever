package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import forever.end.client.ecf.fx.CameraFx;
import net.minecraft.client.Camera;

/**
 * Freelook: после настройки камеры подменяем её поворот на свободный,
 * пока удерживается клавиша (игрок при этом смотрит/двигается в свою сторону).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void ecf$freelook(CallbackInfo ci) {
        if (CameraFx.freelookActive()) {
            setRotation(CameraFx.freeYaw(), CameraFx.freePitch());
        }
    }
}
