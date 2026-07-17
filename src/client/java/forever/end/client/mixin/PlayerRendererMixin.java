package forever.end.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import forever.end.client.ecf.fx.AnimationFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

/**
 * Помечает, что сейчас извлекается render-state именно локального игрока.
 * Сразу после этого вызывается setupAnim той же модели, где флаг и читается
 * (см. {@link forever.end.client.mixin.PlayerEmoteMixin}). Так эмоция накладывается
 * только на модель самого игрока (видно в F5), не трогая чужих игроков и мобов.
 */
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    private void ecf$markLocalPlayer(AbstractClientPlayer entity, PlayerRenderState state, float partialTick, CallbackInfo ci) {
        AnimationFx.animatingLocalPlayer = entity == Minecraft.getInstance().player;
    }
}
