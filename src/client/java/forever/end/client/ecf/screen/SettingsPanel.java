package forever.end.client.ecf.screen;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.setting.Setting;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Плавающая панель настроек модуля (ПКМ по модулю). Цвета — по активной теме. */
public class SettingsPanel {
    private static final int W = 208;
    private static final int HEAD = 28;
    private static final int PAD = 10;

    private final Font font;
    private boolean open = false;
    private Module module;
    private int px, py, ph;

    private final List<Hit> hits = new ArrayList<>();
    private Setting.Num dragNum;
    private int dragNumX, dragNumW;
    private Setting.Color dragHue;
    private int dragHueX, dragHueW;

    public SettingsPanel(Font font) {
        this.font = font;
    }

    public boolean isOpen() { return open; }
    public Module module() { return module; }

    public void close() {
        open = false;
        module = null;
        dragNum = null;
        dragHue = null;
    }

    public void open(Module m, double mx, double my, int screenW, int screenH) {
        if (m == null || !m.hasSettings()) { close(); return; }
        this.module = m;
        this.open = true;
        this.ph = HEAD + PAD;
        for (Setting s : m.settings) ph += rowH(s);
        this.px = (int) Math.min(mx, screenW - W - 8);
        this.py = (int) Math.min(my, screenH - ph - 8);
        if (px < 8) px = 8;
        if (py < 8) py = 8;
    }

    private static int rowH(Setting s) {
        if (s instanceof Setting.Bool) return 22;
        if (s instanceof Setting.Num) return 34;
        if (s instanceof Setting.Mode) return 24;
        if (s instanceof Setting.Color) return 54;
        return 22;
    }

    private Theme t() { return ClientState.theme; }

    // ================= рендер =================
    public void render(GuiGraphics g, int mx, int my) {
        if (!open || module == null) return;
        hits.clear();
        Theme t = t();
        int accent = 0xFF000000 | (t.accent & 0xFFFFFF);

        Draw.roundRect(g, px, py + 4, W, ph, 12, 0x44000000);
        Draw.roundRectBorder(g, px, py, W, ph, 12, t.panel, t.border());
        // шапка
        Draw.roundRect(g, px + PAD, py + 9, 8, 8, 4, accent);
        g.drawString(font, Fonts.display(module.name), px + PAD + 14, py + 9, t.text, false);
        g.fill(px + 8, py + HEAD - 1, px + W - 8, py + HEAD, t.border());

        int y = py + HEAD + 4;
        int rx = px + PAD;
        int rw = W - PAD * 2;
        for (Setting s : module.settings) {
            int h = rowH(s);
            if (s instanceof Setting.Bool b) {
                g.drawString(font, Fonts.body(s.name), rx, y + 6, t.text, false);
                int swW = 26, swH = 14, swX = rx + rw - swW, swY = y + 2;
                Draw.roundRect(g, swX, swY, swW, swH, swH / 2, b.value ? accent : 0xFFC9CCD3);
                int knob = b.value ? swX + swW - swH + 2 : swX + 2;
                Draw.roundRect(g, knob, swY + 2, swH - 4, swH - 4, (swH - 4) / 2, 0xFFFFFFFF);
                hits.add(new Hit(0, s, rx, y, rw, h - 2));
            } else if (s instanceof Setting.Num n) {
                g.drawString(font, Fonts.body(s.name), rx, y + 2, t.text, false);
                String disp = n.display();
                g.drawString(font, Fonts.body(disp), rx + rw - font.width(Fonts.body(disp)), y + 2, t.muted, false);
                int tx = rx, tw = rw, ty = y + 18, th = 5;
                Draw.roundRect(g, tx, ty, tw, th, 2, t.panel2);
                int fill = (int) Math.round(tw * n.norm());
                Draw.roundRect(g, tx, ty, Math.max(2, fill), th, 2, accent);
                int kx = tx + Math.max(0, Math.min(tw - 6, fill - 3));
                Draw.roundRect(g, kx, ty - 2, 6, th + 4, 3, 0xFFFFFFFF);
                hits.add(new Hit(1, s, tx, ty - 4, tw, th + 8));
            } else if (s instanceof Setting.Mode md) {
                g.drawString(font, Fonts.body(s.name), rx, y + 6, t.text, false);
                int aw = 16;
                int rArrowX = rx + rw - aw;
                int lArrowX = rArrowX - 70;
                Draw.roundRect(g, lArrowX, y + 2, aw, 16, 4, t.panel2);
                Draw.roundRect(g, rArrowX, y + 2, aw, 16, 4, t.panel2);
                g.drawString(font, Fonts.body("‹"), lArrowX + 6, y + 6, t.text, false);
                g.drawString(font, Fonts.body("›"), rArrowX + 6, y + 6, t.text, false);
                String v = md.value();
                int vx = lArrowX + aw + (rArrowX - (lArrowX + aw)) / 2 - font.width(Fonts.body(v)) / 2;
                g.drawString(font, Fonts.body(v), vx, y + 6, accent, false);
                hits.add(new Hit(2, s, lArrowX, y + 2, aw, 16));
                hits.add(new Hit(3, s, rArrowX, y + 2, aw, 16));
            } else if (s instanceof Setting.Color c) {
                g.drawString(font, Fonts.body(s.name), rx, y + 2, t.text, false);
                int resolved = Colors.resolve(c);
                Draw.roundRectBorder(g, rx + rw - 26, y + 1, 26, 13, 4, resolved, t.border());
                // кнопки режима
                int by = y + 18, bh = 13;
                int bw = 52;
                int themeSel = c.syncTheme ? accent : t.panel2;
                int rainSel = c.rainbow ? accent : t.panel2;
                Draw.roundRect(g, rx, by, bw, bh, 4, themeSel);
                Draw.roundRect(g, rx + bw + 6, by, bw, bh, 4, rainSel);
                g.drawString(font, Fonts.body("Тема"), rx + 8, by + 3, c.syncTheme ? 0xFFFFFFFF : t.text, false);
                g.drawString(font, Fonts.body("Радуга"), rx + bw + 6 + 6, by + 3, c.rainbow ? 0xFFFFFFFF : t.text, false);
                hits.add(new Hit(4, s, rx, by, bw, bh));
                hits.add(new Hit(5, s, rx + bw + 6, by, bw, bh));
                // hue-полоса
                int hx = rx, hy = y + 36, hw = rw, hh = 10;
                int seg = hw;
                for (int i = 0; i < seg; i++) {
                    g.fill(hx + i, hy, hx + i + 1, hy + hh, Colors.fromHue((float) i / seg));
                }
                boolean manual = !c.syncTheme && !c.rainbow;
                if (manual) {
                    int knobX = hx + (int) (Colors.hueOf(c.argb) * hw);
                    g.fill(knobX - 1, hy - 2, knobX + 2, hy + hh + 2, 0xFFFFFFFF);
                }
                hits.add(new Hit(6, s, hx, hy - 2, hw, hh + 4));
            }
            y += h;
        }
    }

    // ================= ввод =================
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!open) return false;
        if (!inside(mx, my)) { close(); return false; }
        if (btn != 0) return true;
        for (Hit h : hits) {
            if (mx >= h.x && mx <= h.x + h.w && my >= h.y && my <= h.y + h.h) {
                apply(h, mx);
                return true;
            }
        }
        return true;
    }

    private void apply(Hit h, double mx) {
        switch (h.type) {
            case 0 -> { if (h.s instanceof Setting.Bool b) b.value = !b.value; }
            case 1 -> { if (h.s instanceof Setting.Num n) { dragNum = n; dragNumX = h.x; dragNumW = h.w; setNum(mx); } }
            case 2 -> { if (h.s instanceof Setting.Mode m) m.cycle(-1); }
            case 3 -> { if (h.s instanceof Setting.Mode m) m.cycle(1); }
            case 4 -> { if (h.s instanceof Setting.Color c) { c.syncTheme = true; c.rainbow = false; } }
            case 5 -> { if (h.s instanceof Setting.Color c) { c.rainbow = true; c.syncTheme = false; } }
            case 6 -> { if (h.s instanceof Setting.Color c) { c.syncTheme = false; c.rainbow = false; dragHue = c; dragHueX = h.x; dragHueW = h.w; setHue(mx); } }
            default -> {}
        }
    }

    public boolean mouseDragged(double mx, double my, int btn) {
        if (!open) return false;
        boolean did = false;
        if (dragNum != null) { setNum(mx); did = true; }
        if (dragHue != null) { setHue(mx); did = true; }
        return did;
    }

    public boolean mouseReleased(double mx, double my, int btn) {
        dragNum = null;
        dragHue = null;
        return false;
    }

    private void setNum(double mx) {
        if (dragNum == null || dragNumW <= 0) return;
        dragNum.setFromNorm((mx - dragNumX) / dragNumW);
    }

    private void setHue(double mx) {
        if (dragHue == null || dragHueW <= 0) return;
        float hue = (float) Math.max(0.0, Math.min(1.0, (mx - dragHueX) / dragHueW));
        dragHue.argb = Colors.fromHue(hue);
    }

    private boolean inside(double mx, double my) {
        return mx >= px && mx <= px + W && my >= py && my <= py + ph;
    }

    private static final class Hit {
        final int type;
        final Setting s;
        final int x, y, w, h;

        Hit(int type, Setting s, int x, int y, int w, int h) {
            this.type = type;
            this.s = s;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
