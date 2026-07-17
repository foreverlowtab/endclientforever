package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import forever.end.client.ecf.fx.AnimationFx;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Накладывает позу эмоции на модель локального игрока после ванильной раскладки.
 * Целимся в HumanoidModel#setupAnim (TAIL): после нашей правки PlayerModel сам копирует
 * внешние слои (рукава/куртка/штаны/шляпа) с основных частей, поэтому поза выглядит цельной.
 */
@Mixin(HumanoidModel.class)
public class PlayerEmoteMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void ecf$emotePose(HumanoidRenderState state, CallbackInfo ci) {
        if (((Object) this) instanceof PlayerModel model && AnimationFx.animatingLocalPlayer) {
            AnimationFx.applyEmotePose(model);
        }
    }
}
