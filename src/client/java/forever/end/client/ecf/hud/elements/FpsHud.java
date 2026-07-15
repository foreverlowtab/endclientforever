package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudData;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** FPS: метка + число (цвет по значению) + мини-график истории. */
public class FpsHud extends HudElement {
    private static final int BARS = 40;
    private Component label, value;
    private int labelW, valueW, fps;
    private boolean graph;

    public FpsHud() { super("fps", "FPS", "FPS", 0.008f, 0.11f); }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        graph = opt("\u0413\u0440\u0430\u0444\u0438\u043a", true);
        fps = mc.getFps();
        label = Fonts.body("FPS");
        value = Fonts.display(String.valueOf(fps));
        labelW = f.width(label);
        valueW = f.width(value);
        int row = labelW + 5 + valueW;
        w = 8 + Math.max(row, graph ? BARS : 0) + 8;
        h = 6 + 10 + (graph ? 4 + 14 : 0) + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        HudManager.glass(g, 0, 0, w, h);
        int x = 8, y = 6;
        g.drawString(f, label, x, y + 1, HudManager.MUTED, false);
        g.drawString(f, value, x + labelW + 5, y, HudManager.fpsColor(fps), true);
        if (graph) {
            int gx = 8, gy = y + 12, gh = 14, gw = w - 16;
            int max = 60;
            for (int i = 0; i < BARS; i++) max = Math.max(max, HudData.fpsAt(HudData.LEN - BARS + i));
            int bw = Math.max(1, gw / BARS);
            for (int i = 0; i < BARS; i++) {
                int v = HudData.fpsAt(HudData.LEN - BARS + i);
                int bh = Math.max(1, Math.round((float) v / max * gh));
                int bx = gx + i * bw;
                g.fill(bx, gy + gh - bh, bx + bw - (bw > 1 ? 1 : 0), gy + gh, HudManager.fpsColor(v));
            }
        }
    }
}
