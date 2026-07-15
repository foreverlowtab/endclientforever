package forever.end.client.ecf.hud;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.EcfConfig;
import forever.end.client.ecf.hud.elements.ArmorHudElement;
import forever.end.client.ecf.hud.elements.ClockHud;
import forever.end.client.ecf.hud.elements.CoordinatesHud;
import forever.end.client.ecf.hud.elements.CpsHud;
import forever.end.client.ecf.hud.elements.FpsHud;
import forever.end.client.ecf.hud.elements.KeystrokesHud;
import forever.end.client.ecf.hud.elements.PotionHudElement;
import forever.end.client.ecf.hud.elements.WatermarkHud;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Реестр и рендер HUD-элементов категории HUD. */
public final class HudManager {
    private HudManager() {}

    // Стекло HUD-карточек (тёмный полупрозрачный фон + тонкий верхний блик).
    public static final int BG = 0xB0121417;
    public static final int TXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFFAEB4C0;

    private static final List<HudElement> ELEMENTS = new ArrayList<>();
    private static boolean built = false;

    public static List<HudElement> elements() {
        build();
        return ELEMENTS;
    }

    private static void build() {
        if (built) return;
        built = true;
        ELEMENTS.add(new WatermarkHud());
        ELEMENTS.add(new FpsHud());
        ELEMENTS.add(new CpsHud());
        ELEMENTS.add(new KeystrokesHud());
        ELEMENTS.add(new CoordinatesHud());
        ELEMENTS.add(new ArmorHudElement());
        ELEMENTS.add(new PotionHudElement());
        ELEMENTS.add(new ClockHud());
        EcfConfig.loadHud(ELEMENTS);
    }

    /** Игровой рендер (только включённые модули). */
    public static void renderGame(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        build();
        HudData.update(mc);
        for (HudElement e : ELEMENTS) {
            if (e.enabled()) e.render(g, mc, partial, false);
        }
    }

    // --- общие хелперы отрисовки карточек ---
    public static void glass(GuiGraphics g, int x, int y, int w, int h) {
        Draw.roundRect(g, x, y, w, h, 4, BG);
        g.fill(x + 4, y, x + w - 4, y + 1, 0x1AFFFFFF);
    }

    public static void glassAccent(GuiGraphics g, int x, int y, int w, int h, int accent) {
        glass(g, x, y, w, h);
        Draw.roundRect(g, x, y, 2, h, 1, accent);
    }

    public static int accent() {
        return Colors.themeAccent();
    }

    public static int fpsColor(int fps) {
        if (fps >= 60) return 0xFF3FB950;
        if (fps >= 30) return Colors.themeAccent();
        return 0xFFE5484D;
    }
}
