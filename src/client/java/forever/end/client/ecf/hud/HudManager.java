package forever.end.client.ecf.hud;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import forever.end.client.ecf.ClientState;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Менеджер HUD: собирает элементы, рисует их в игре и содержит общие хелперы
 * отрисовки (стеклянные карточки и масштабируемый текст) — стиль портирован 1:1 с hud.html.
 */
public final class HudManager {
    private HudManager() {}

    // Цвета стеклянного HUD (порт из .glass / .hud).
    public static final int BG = 0x6B0A0C14;       // rgba(10,12,20,.42)
    public static final int BORDER = 0x24FFFFFF;   // rgba(255,255,255,.14)
    public static final int TXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFFC6CBD8;    // #c6cbd8
    public static final int MUTED2 = 0xFFAEB4C4;   // #aeb4c4
    public static final int ONLINE = 0xFF41D18A;   // #41d18a
    public static final int VER = 0xFFCDD2E0;      // #cdd2e0
    public static final int TRACK = 0x29FFFFFF;    // rgba(255,255,255,.16) — фон прогресс-бара

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
        ELEMENTS.add(new PotionHudElement());
        ELEMENTS.add(new KeystrokesHud());
        ELEMENTS.add(new CoordinatesHud());
        ELEMENTS.add(new ArmorHudElement());
        ELEMENTS.add(new ClockHud());
        EcfConfig.loadHud(ELEMENTS);
    }

    public static void renderGame(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        build();
        HudData.update(mc);
        for (HudElement e : ELEMENTS) {
            if (e.enabled()) e.render(g, mc, partial, false);
        }
    }

    // ===== Общие цвета темы =====
    public static int accent() { return Colors.themeAccent(); }

    public static int accent2() { return Colors.themeAccent2(); }

    public static int accentSoft(int a) { return (accent() & 0x00FFFFFF) | ((a & 0xFF) << 24); }

    // ===== Стеклянная карточка =====
    /** Тёмный фон + тонкая рамка + акцентная полоса слева (как .glass). */
    public static void card(GuiGraphics g, int x, int y, int w, int h) {
        Draw.roundRect(g, x, y, w, h, 4, BORDER);
        Draw.roundRect(g, x + 1, y + 1, w - 2, h - 2, 3, BG);
        Draw.roundRect(g, x, y + 2, 2, h - 4, 1, accent());
    }

    /** Карточка без акцентной полосы (для клавиш/брони). */
    public static void chip(GuiGraphics g, int x, int y, int w, int h, int r, int bg, int border) {
        Draw.roundRect(g, x, y, w, h, r, border);
        Draw.roundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), bg);
    }

    // ===== Текст с масштабом =====
    public static void text(GuiGraphics g, Font font, Component c, float x, float y, float sc, int color) {
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 0);
        ps.scale(sc, sc, 1f);
        g.drawString(font, c, 0, 0, color, true);
        ps.popPose();
    }

    public static void textCenter(GuiGraphics g, Font font, Component c, float cx, float y, float sc, int color) {
        float w = font.width(c) * sc;
        text(g, font, c, cx - w / 2f, y, sc, color);
    }

    public static void textRight(GuiGraphics g, Font font, Component c, float rightX, float y, float sc, int color) {
        float w = font.width(c) * sc;
        text(g, font, c, rightX - w, y, sc, color);
    }

    public static float tw(Font font, Component c, float sc) {
        return font.width(c) * sc;
    }

    /** Цвет FPS: зелёный / акцент / красный. */
    public static int fpsColor(int fps) {
        if (fps >= 60) return 0xFF3FB950;
        if (fps >= 30) return accent();
        return 0xFFE5484D;
    }
}
