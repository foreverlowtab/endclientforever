package forever.end.client.ecf.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Хелперы отрисовки: скруглённые панели, градиенты, границы (порт стилей из HTML). */
public final class Draw {
    private Draw() {}

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, color);
    }

    public static void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        rect(g, x, y, w, h, fill);
        border(g, x, y, w, h, border);
    }

    /** Заливка со скруглёнными углами (радиус r). */
    public static void roundRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double d = Math.sqrt((double) (r * r - (r - i) * (r - i)));
            int off = r - (int) Math.floor(d);
            int ry1 = y + i;
            int ry2 = y + h - 1 - i;
            g.fill(x + off, ry1, x + r, ry1 + 1, color);
            g.fill(x + w - r, ry1, x + w - off, ry1 + 1, color);
            g.fill(x + off, ry2, x + r, ry2 + 1, color);
            g.fill(x + w - r, ry2, x + w - off, ry2 + 1, color);
        }
    }

    /** Скруглённая панель с 1px границей. */
    public static void roundRectBorder(GuiGraphics g, int x, int y, int w, int h, int r, int fill, int border) {
        roundRect(g, x, y, w, h, r, border);
        roundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    /** Полностью скруглённая "таблетка". */
    public static void pill(GuiGraphics g, int x, int y, int w, int h, int fill) {
        roundRect(g, x, y, w, h, h / 2, fill);
    }

    public static void pillBorder(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        roundRectBorder(g, x, y, w, h, h / 2, fill, border);
    }

    /** Вертикальный градиент. */
    public static void gradientV(GuiGraphics g, int x, int y, int w, int h, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        g.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    /** Установить альфу для ARGB-цвета (0..255). */
    public static int alpha(int color, int a) {
        return (color & 0x00FFFFFF) | ((a & 0xFF) << 24);
    }

    /** Линейная интерполяция двух ARGB-цветов (t = 0..1). */
    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int na = (int) (aa + (ba - aa) * t);
        int nr = (int) (ar + (br - ar) * t);
        int ng = (int) (ag + (bg - ag) * t);
        int nb = (int) (ab + (bb - ab) * t);
        return (na << 24) | (nr << 16) | (ng << 8) | nb;
    }
}
