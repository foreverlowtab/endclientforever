package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Keystrokes: WASD + пробел + кнопки мыши, подсветка при нажатии. */
public class KeystrokesHud extends HudElement {
    private static final int KEY = 15, GAP = 2;
    private boolean space, mouse;

    public KeystrokesHud() { super("keystrokes", "Keystrokes", "Keystrokes", 0.02f, 0.55f); }

    @Override
    protected void layout(Minecraft mc) {
        space = opt("\u041f\u0440\u043e\u0431\u0435\u043b", true);
        mouse = opt("\u041a\u043d\u043e\u043f\u043a\u0438 \u043c\u044b\u0448\u0438", true);
        w = 8 + (KEY * 3 + GAP * 2) + 8;
        int rows = 12 + (KEY + GAP) + KEY; // W row + ASD row
        if (mouse) rows += GAP + 12;
        if (space) rows += GAP + 9;
        h = 6 + rows + 6;
    }

    private void cap(GuiGraphics g, Font f, int x, int y, int cw, int ch, String s, boolean down) {
        int acc = Colors.themeAccent();
        Draw.roundRect(g, x, y, cw, ch, 3, down ? acc : HudManager.BG);
        if (!down) g.fill(x + 3, y, x + cw - 3, y + 1, 0x18FFFFFF);
        Component c = Fonts.body(s);
        g.drawString(f, c, x + (cw - f.width(c)) / 2, y + (ch - 8) / 2, down ? 0xFFFFFFFF : HudManager.MUTED, false);
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        var o = mc.options;
        int x0 = 8, y = 6;
        int col2 = x0 + KEY + GAP;
        // W
        cap(g, f, col2, y, KEY, KEY, "W", editor ? false : o.keyUp.isDown());
        int y2 = y + KEY + GAP;
        cap(g, f, x0, y2, KEY, KEY, "A", editor ? false : o.keyLeft.isDown());
        cap(g, f, col2, y2, KEY, KEY, "S", editor ? false : o.keyDown.isDown());
        cap(g, f, x0 + (KEY + GAP) * 2, y2, KEY, KEY, "D", editor ? false : o.keyRight.isDown());
        int yy = y2 + KEY;
        int full = KEY * 3 + GAP * 2;
        if (mouse) {
            yy += GAP;
            int half = (full - GAP) / 2;
            cap(g, f, x0, yy, half, 12, "LMB", editor ? false : o.keyAttack.isDown());
            cap(g, f, x0 + half + GAP, yy, full - half - GAP, 12, "RMB", editor ? false : o.keyUse.isDown());
            yy += 12;
        }
        if (space) {
            yy += GAP;
            cap(g, f, x0, yy, full, 9, "\u2500\u2500\u2500", editor ? false : o.keyJump.isDown());
        }
    }
}
