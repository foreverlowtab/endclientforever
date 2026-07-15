package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

/** Активные эффекты: карточка с иконкой, уровнем, прогресс-баром и таймером — порт .hud-pot. Справа. */
public class PotionHudElement extends HudElement {
    public PotionHudElement() { super("pot", "Potion HUD", "Potion HUD", Anchor.TR, -6, 104); }

    @Override
    protected void layout(Minecraft mc) {
        int n = mc.player == null ? 0 : mc.player.getActiveEffects().size();
        this.w = 104;
        this.h = n == 0 ? 1 : n * 26 + (n - 1) * 4;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        if (mc.player == null) return;
        var effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) return;
        boolean showTime = opt("\u0412\u0440\u0435\u043c\u044f", true);
        Font f = mc.font;
        int y = 0;
        for (MobEffectInstance eff : effects) {
            pot(g, f, eff, y, showTime);
            y += 30;
        }
    }

    private void pot(GuiGraphics g, Font f, MobEffectInstance eff, int y, boolean showTime) {
        int ch = 26;
        HudManager.card(g, 0, y, w, ch);
        int col = catColor(eff);
        HudManager.chip(g, 6, y + 5, 16, 16, 3, (col & 0x00FFFFFF) | 0x55000000, col);
        Component name = eff.getEffect().value().getDisplayName();
        HudManager.text(g, f, name, 26, y + 4, 0.85f, HudManager.TXT);
        String amp = roman(eff.getAmplifier() + 1);
        if (!amp.isEmpty()) HudManager.textRight(g, f, Fonts.body(amp), w - 7, y + 4, 0.85f, HudManager.MUTED);
        int bx = 26, bw = w - 33, by = y + 14;
        float frac = eff.isInfiniteDuration() ? 1f : Math.min(1f, eff.getDuration() / 6000f);
        Draw.roundRect(g, bx, by, bw, 3, 1, HudManager.TRACK);
        Draw.roundRect(g, bx, by, Math.max(1, Math.round(bw * frac)), 3, 1, col);
        if (showTime) {
            String t = eff.isInfiniteDuration() ? "\u221e" : mmss(eff.getDuration() / 20);
            HudManager.text(g, f, Fonts.body(t), 26, y + 18, 0.8f, HudManager.MUTED);
        }
    }

    private static int catColor(MobEffectInstance eff) {
        var c = eff.getEffect().value().getCategory();
        return switch (c) {
            case BENEFICIAL -> 0xFF8FD6A8;
            case HARMFUL -> 0xFFE5484D;
            default -> 0xFF7CC4FF;
        };
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            default -> n <= 0 ? "" : Integer.toString(n);
        };
    }

    private static String mmss(int totalSec) {
        int m = totalSec / 60, s = totalSec % 60;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }
}
