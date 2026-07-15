package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudData;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** FPS + живой мини-график (вертикальные столбцы) — порт .hud-fps. Верхний правый угол. */
public class FpsHud extends HudElement {
    private static final int BARS = 16;

    public FpsHud() { super("fps", "FPS", "FPS", Anchor.TR, -6, 6); }

    @Override
    protected void layout(Minecraft mc) {
        this.w = 74;
        this.h = opt("\u0413\u0440\u0430\u0444\u0438\u043a", true) ? 40 : 24;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        boolean graph = opt("\u0413\u0440\u0430\u0444\u0438\u043a", true);
        HudManager.card(g, 0, 0, w, h);
        Font f = mc.font;
        int fps = mc.getFps();
        HudManager.text(g, f, Fonts.grotesk(Integer.toString(fps)), 7, 6, 1.7f, HudManager.fpsColor(fps));
        HudManager.textRight(g, f, Fonts.body("FPS"), w - 7, 10, 0.85f, HudManager.MUTED);
        if (graph) {
            int gx = 7, gy = 24, gw = w - 14, gh = 11, gap = 1;
            float bw = (gw - gap * (BARS - 1)) / (float) BARS;
            int max = 60;
            for (int i = 0; i < BARS; i++) {
                int v = HudData.fpsAt(HudData.LEN - BARS + i);
                if (v > max) max = v;
            }
            for (int i = 0; i < BARS; i++) {
                int v = HudData.fpsAt(HudData.LEN - BARS + i);
                float frac = Math.max(0.12f, Math.min(1f, v / (float) max));
                int bh = Math.max(1, Math.round(gh * frac));
                int bx = Math.round(gx + i * (bw + gap));
                int bxe = Math.round(gx + i * (bw + gap) + bw);
                if (bxe <= bx) bxe = bx + 1;
                g.fillGradient(bx, gy + gh - bh, bxe, gy + gh, HudManager.accent2(), HudManager.accent());
            }
        }
    }
}
