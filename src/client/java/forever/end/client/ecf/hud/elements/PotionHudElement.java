package forever.end.client.ecf.hud.elements;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

/** Potion HUD: активные эффекты (точка / название + уровень / таймер). */
public class PotionHudElement extends HudElement {
    private boolean showTime;
    private final List<Row> rows = new ArrayList<>();

    private record Row(Component name, Component time) {}

    public PotionHudElement() { super("potion", "Potion HUD", "Potion HUD", 0.86f, 0.12f); }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "";
            case 2 -> " II";
            case 3 -> " III";
            case 4 -> " IV";
            case 5 -> " V";
            default -> " " + n;
        };
    }

    private static String fmt(int ticks) {
        int sec = ticks / 20;
        int m = sec / 60, s = sec % 60;
        return String.format("%d:%02d", m, s);
    }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        showTime = opt("\u0412\u0440\u0435\u043c\u044f", true);
        rows.clear();
        if (mc.player != null) {
            for (MobEffectInstance mi : mc.player.getActiveEffects()) {
                String nm = mi.getEffect().value().getDisplayName().getString() + roman(mi.getAmplifier() + 1);
                String tm = mi.isInfiniteDuration() ? "\u221e" : fmt(mi.getDuration());
                rows.add(new Row(Fonts.body(nm), Fonts.body(tm)));
            }
        }
        int maxName = 0, maxTime = 0;
        for (Row r : rows) {
            maxName = Math.max(maxName, f.width(r.name()));
            if (showTime) maxTime = Math.max(maxTime, f.width(r.time()));
        }
        int n = rows.size();
        if (n == 0) {
            if (editing) { w = 100; h = 24; } else { w = 0; h = 0; }
            return;
        }
        w = 8 + 5 + 5 + maxName + (showTime ? 8 + maxTime : 0) + 8;
        h = 6 + n * 11 + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        int acc = Colors.themeAccent();
        if (rows.isEmpty()) {
            if (editor) {
                HudManager.glass(g, 0, 0, Math.max(w, 100), Math.max(h, 24));
                g.drawString(f, Fonts.body("Potion HUD"), 8, 8, HudManager.MUTED, false);
            }
            return;
        }
        HudManager.glassAccent(g, 0, 0, w, h, acc);
        int y = 6;
        for (Row r : rows) {
            Draw.roundRect(g, 8, y + 2, 5, 5, 2, acc);
            g.drawString(f, r.name(), 8 + 5 + 5, y + 1, 0xFFFFFFFF, false);
            if (showTime) {
                int tw = f.width(r.time());
                g.drawString(f, r.time(), w - 8 - tw, y + 1, HudManager.MUTED, false);
            }
            y += 11;
        }
    }
}
