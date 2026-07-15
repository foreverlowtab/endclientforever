package forever.end.client.ecf.module;

import forever.end.client.ecf.fx.EffectFx;
import forever.end.client.ecf.hud.HudManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionResult;

/** Регистрация событий Fabric и диспетчеризация хуков включённых модулей. */
public final class ModuleManager {
    private ModuleManager() {}

    private static boolean inited = false;

    public static void init() {
        if (inited) return;
        inited = true;

        // Рендер в мире (косметика + эффекты).
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            boolean any = false;
            for (Category c : Modules.CATEGORIES) {
                for (Module m : c.modules) {
                    if (m.enabled) { m.doWorld(ctx); any = true; }
                }
            }
            if (any) {
                MultiBufferSource buf = ctx.consumers();
                if (buf instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
            }
        });

        // HUD (вспышки урона/удара и т.п.).
        HudRenderCallback.EVENT.register((g, delta) -> {
            float partial = delta.getGameTimeDeltaPartialTick(false);
            for (Category c : Modules.CATEGORIES) {
                for (Module m : c.modules) {
                    if (m.enabled) m.doHud(g, partial);
                }
            }
            // HUD-элементы категории HUD (с позициями/масштабом из редактора).
            HudManager.renderGame(g, partial);
        });

        // Клиентский тик (логика эффектов).
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.level == null || mc.player == null) return;
            for (Category c : Modules.CATEGORIES) {
                for (Module m : c.modules) {
                    if (m.enabled) m.doTick(mc);
                }
            }
        });

        // Атака по сущности (Hit / Kill Effect).
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide && player == Minecraft.getInstance().player) {
                EffectFx.onAttack(entity);
            }
            return InteractionResult.PASS;
        });
    }
}
