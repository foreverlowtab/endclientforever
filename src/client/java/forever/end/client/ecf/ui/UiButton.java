package forever.end.client.ecf.ui;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Кастомная кнопка в стиле HTML-прототипа (accent-таблетки, меню-кнопки, иконки). */
public class UiButton extends AbstractButton {
    public enum Style { PRIMARY, GHOST, MENU, MENU_PRIMARY, ICON_CIRCLE, SEG_ACTIVE, SEG_INACTIVE }

    private final Runnable action;
    private Style style;
    private String icon = "";
    private String kbd = "";
    private final Font font;
    private float hoverT = 0f;

    public UiButton(int x, int y, int w, int h, Component msg, Style style, Runnable action) {
        super(x, y, w, h, msg);
        this.style = style;
        this.action = action;
        this.font = Minecraft.getInstance().font;
    }

    public UiButton icon(String s) { this.icon = s == null ? "" : s; return this; }
    public UiButton kbd(String s) { this.kbd = s == null ? "" : s; return this; }
    public void setStyle(Style s) { this.style = s; }

    @Override
    public void onPress() {
        if (action != null) action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }

    private void text(GuiGraphics g, String s, int cx, int cy, int color) {
        g.drawString(font, s, cx - font.width(s) / 2, cy - 4, color, false);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float pt) {
        Theme t = ClientState.theme;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hov = isHovered() && this.active;
        hoverT += ((hov ? 1f : 0f) - hoverT) * 0.28f;
        float ht = hoverT;
        int cy = y + h / 2;
        String msg = getMessage().getString();

        switch (style) {
            case PRIMARY: {
                int yo = (int) (-2 * ht);
                int base = Draw.lerpColor(t.accent, t.accent2, ht);
                Draw.roundRect(g, x, y + 3 + yo, w, h, h / 2, Draw.alpha(t.accent, 0x3A));
                Draw.roundRect(g, x, y + yo, w, h, h / 2, this.active ? base : Draw.alpha(base, 0x88));
                text(g, msg, x + w / 2, cy + yo, 0xFFFFFFFF);
                break;
            }
            case GHOST: {
                int yo = (int) (-2 * ht);
                int bd = Draw.lerpColor(t.border(), t.accent, ht);
                Draw.roundRectBorder(g, x, y + yo, w, h, h / 2, t.panel2, bd);
                text(g, msg, x + w / 2, cy + yo, Draw.lerpColor(t.text, t.accent, ht));
                break;
            }
            case MENU:
            case MENU_PRIMARY: {
                boolean prim = style == Style.MENU_PRIMARY;
                int xo = (int) (6 * ht);
                int bx = x + xo;
                int bg, bd, lbl, icoBg, icoFg;
                if (prim) {
                    bg = Draw.lerpColor(t.accent, t.accent2, ht);
                    bd = bg;
                    lbl = 0xFFFFFFFF;
                    icoBg = 0x33FFFFFF;
                    icoFg = 0xFFFFFFFF;
                } else {
                    bg = Draw.lerpColor(t.panel, t.accentSoft(), ht);
                    bd = Draw.lerpColor(t.border(), t.accent, ht);
                    lbl = Draw.lerpColor(t.text, t.accent, ht);
                    icoBg = Draw.lerpColor(t.accentSoft(), t.accent, ht);
                    icoFg = Draw.lerpColor(t.accent, 0xFFFFFFFF, ht);
                }
                Draw.roundRectBorder(g, bx, y, w, h, 7, bg, bd);
                int is = h - 12;
                int iy = y + 6;
                int ix = bx + 8;
                Draw.roundRect(g, ix, iy, is, is, 5, icoBg);
                if (!icon.isEmpty()) g.drawString(font, icon, ix + (is - font.width(icon)) / 2, iy + is / 2 - 4, icoFg, false);
                g.drawString(font, msg, ix + is + 10, cy - 4, lbl, false);
                if (!kbd.isEmpty()) {
                    int kw = font.width(kbd) + 10, kh = 14;
                    int kx = bx + w - 8 - kw, ky = y + (h - kh) / 2;
                    int kbg = prim ? 0x22FFFFFF : t.panel2;
                    int kbd2 = prim ? 0x55FFFFFF : t.border();
                    int kfg = prim ? 0xFFFFFFFF : Draw.lerpColor(t.muted, t.accent, ht);
                    Draw.roundRectBorder(g, kx, ky, kw, kh, 4, kbg, kbd2);
                    g.drawString(font, kbd, kx + 5, ky + kh / 2 - 4, kfg, false);
                }
                break;
            }
            case ICON_CIRCLE: {
                int bg = Draw.lerpColor(t.panel2, t.accent, ht);
                int fg = Draw.lerpColor(t.muted, 0xFFFFFFFF, ht);
                Draw.roundRect(g, x, y, w, h, h / 2, bg);
                text(g, msg, x + w / 2, cy, fg);
                break;
            }
            case SEG_ACTIVE: {
                Draw.roundRect(g, x, y, w, h, h / 2, 0xFFFFFFFF);
                text(g, msg, x + w / 2, cy, t.accent);
                break;
            }
            case SEG_INACTIVE: {
                text(g, msg, x + w / 2, cy, Draw.alpha(0xFFFFFFFF, hov ? 0xFF : 0xCC));
                break;
            }
        }
    }
}
