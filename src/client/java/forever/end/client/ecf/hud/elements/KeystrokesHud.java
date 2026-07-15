package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudData;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Клавиши WASD + пробел (со свечением при нажатии и счётчиком) — порт .hud-keys. Низ слева. */
public class KeystrokesHud extends HudElement {
    public KeystrokesHud() { super("keys", "Keystrokes", "Keystrokes", Anchor.BL, 6, -52); }

    @Override
    protected void layout(Minecraft mc) {
        this.w = 52;
        int hh = 34;
        if (opt("\u041f\u0440\u043e\u0431\u0435\u043b", true)) hh += 12;
        if (opt("\u041a\u043d\u043e\u043f\u043a\u0438 \u043c\u044b\u0448\u0438", true)) hh += 14;
        this.h = hh;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        keyBox(g, f, 18, 0, 16, 16, "W", HudData.kW, HudData.cW);
        keyBox(g, f, 0, 18, 16, 16, "A", HudData.kA, HudData.cA);
        keyBox(g, f, 18, 18, 16, 16, "S", HudData.kS, HudData.cS);
        keyBox(g, f, 36, 18, 16, 16, "D", HudData.kD, HudData.cD);
        int y = 36;
        if (opt("\u041f\u0440\u043e\u0431\u0435\u043b", true)) {
            wideBox(g, f, 0, y, 52, 10, "SPACE", HudData.kSpace);
            y += 12;
        }
        if (opt("\u041a\u043d\u043e\u043f\u043a\u0438 \u043c\u044b\u0448\u0438", true)) {
            wideBox(g, f, 0, y, 25, 12, "\u041b\u041a\u041c", HudData.leftCps() > 0);
            wideBox(g, f, 27, y, 25, 12, "\u041f\u041a\u041c", HudData.rightCps() > 0);
        }
    }

    private void keyBox(GuiGraphics g, Font f, int x, int y, int w, int h, String label, boolean down, int count) {
        base(g, x, y, w, h, down);
        HudManager.textCenter(g, f, Fonts.grotesk(label), x + w / 2f, y + h / 2f - 3, 1.0f, down ? 0xFFFFFFFF : HudManager.MUTED);
        if (count > 0) HudManager.textRight(g, f, Fonts.body(Integer.toString(count)), x + w - 2, y + h - 6, 0.55f, 0xFFB9BEC9);
    }

    private void wideBox(GuiGraphics g, Font f, int x, int y, int w, int h, String label, boolean down) {
        base(g, x, y, w, h, down);
        HudManager.textCenter(g, f, Fonts.body(label), x + w / 2f, y + h / 2f - 2, 0.7f, down ? 0xFFFFFFFF : HudManager.MUTED);
    }

    private void base(GuiGraphics g, int x, int y, int w, int h, boolean down) {
        if (down) {
            Draw.roundRect(g, x, y, w, h, 3, HudManager.accent2());
            Draw.roundRect(g, x + 1, y + 1, w - 2, h - 2, 2, HudManager.accent());
        } else {
            HudManager.chip(g, x, y, w, h, 3, HudManager.BG, HudManager.BORDER);
        }
    }
}
