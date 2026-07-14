package forever.end.client.ecf.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Маленькие хелперы для отрисовки панелей/границ. */
public final class Draw {
    private Draw() {}

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int color) {
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
}
