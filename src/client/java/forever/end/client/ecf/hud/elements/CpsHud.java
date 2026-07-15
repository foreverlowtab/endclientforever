package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudData;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** CPS: ЛКМ / ПКМ — порт .hud-cps. Под FPS справа. */
public class CpsHud extends HudElement {
    public CpsHud() { super("cps", "CPS", "CPS", Anchor.TR, -6, 58); }

    @Override
    protected void layout(Minecraft mc) {
        this.w = 74;
        this.h = opt("\u041b\u041a\u041c+\u041f\u041a\u041c", true) ? 30 : 19;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        boolean both = opt("\u041b\u041a\u041c+\u041f\u041a\u041c", true);
        HudManager.card(g, 0, 0, w, h);
        Font f = mc.font;
        HudManager.text(g, f, Fonts.body("\u041b\u041a\u041c"), 8, 6, 0.9f, HudManager.MUTED2);
        HudManager.textRight(g, f, Fonts.grotesk(Integer.toString(HudData.leftCps())), w - 7, 5, 1.0f, HudManager.TXT);
        if (both) {
            HudManager.text(g, f, Fonts.body("\u041f\u041a\u041c"), 8, 18, 0.9f, HudManager.MUTED2);
            HudManager.textRight(g, f, Fonts.grotesk(Integer.toString(HudData.rightCps())), w - 7, 17, 1.0f, HudManager.TXT);
        }
    }
}
