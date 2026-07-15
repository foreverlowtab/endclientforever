package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudData;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** CPS: клики в секунду (ЛКМ, опционально ПКМ). */
public class CpsHud extends HudElement {
    private Component label, value, right;
    private int labelW, valueW, rightW;
    private boolean both;

    public CpsHud() { super("cps", "CPS", "CPS", 0.008f, 0.19f); }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        both = opt("\u041b\u041a\u041c+\u041f\u041a\u041c", true);
        label = Fonts.body("CPS");
        value = Fonts.display(String.valueOf(HudData.leftCps()));
        right = Fonts.body("R " + HudData.rightCps());
        labelW = f.width(label);
        valueW = f.width(value);
        rightW = f.width(right);
        int row = labelW + 5 + valueW + (both ? 6 + rightW : 0);
        w = 8 + row + 8;
        h = 6 + 10 + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        HudManager.glass(g, 0, 0, w, h);
        int x = 8, y = 6;
        g.drawString(f, label, x, y + 1, HudManager.MUTED, false);
        int vx = x + labelW + 5;
        g.drawString(f, value, vx, y, Colors.themeAccent(), true);
        if (both) {
            g.drawString(f, right, vx + valueW + 6, y + 1, HudManager.MUTED, false);
        }
    }
}
