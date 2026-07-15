package forever.end.client.ecf.screen;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Category;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Внутриигровой ClickGUI (дизайн 1:1: таблетка-бар, карточки категорий, тогглы). */
public class ClickGuiScreen extends Screen {
    private final Screen parent;
    private EditBox search;
    private String query = "";
    private final List<Entry> entries = new ArrayList<>();

    private int barX, barY, barW, barH, searchX, searchW;

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("ClickGUI"));
        this.parent = parent;
    }

    private Theme theme() {
        return ClientState.theme;
    }

    @Override
    protected void init() {
        barW = 460;
        barH = 34;
        barX = (this.width - barW) / 2;
        barY = 20;
        searchW = 190;
        searchX = barX + barW - 8 - 34 - 8 - searchW;

        search = new EditBox(this.font, searchX + 12, barY + barH / 2 - 6, searchW - 24, 12, Component.literal("search"));
        search.setHint(Component.literal("Поиск модуля…"));
        search.setBordered(false);
        search.setTextColor(theme().text);
        search.setResponder(s -> query = s.toLowerCase());
        addRenderableWidget(search);

        addRenderableWidget(new UiButton(barX + barW - 8 - 26, barY + (barH - 26) / 2, 26, 26,
                Component.literal("✕"), UiButton.Style.ICON_CIRCLE, this::onClose));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // Полностью непрозрачный тёмный фон — родительское меню НЕ просвечивает.
        g.fillGradient(0, 0, this.width, this.height, 0xFF0B0C0E, 0xFF060708);
        Theme t = theme();

        // верхняя таблетка-бар
        Draw.pillBorder(g, barX, barY, barW, barH, t.panel, t.border());
        Draw.roundRect(g, barX + 16, barY + barH / 2 - 4, 8, 8, 4, t.accent);
        g.drawString(this.font, Fonts.display("ClickGUI"), barX + 30, barY + barH / 2 - 4, t.text, false);
        Draw.roundRectBorder(g, searchX, barY + 6, searchW, barH - 12, (barH - 12) / 2, t.panel2,
                search != null && search.isFocused() ? t.accent : t.border());

        // доска категорий (до 5 колонок, как в макете)
        entries.clear();
        int top = barY + barH + 16;
        int side = 40, gap = 12;
        int cols = 5;
        int avail = this.width - side * 2;
        while (cols > 1 && (avail - gap * (cols - 1)) / cols < 168) cols--;
        int colW = (avail - gap * (cols - 1)) / cols;
        int x0 = (this.width - (cols * colW + (cols - 1) * gap)) / 2;
        int[] colY = new int[cols];
        for (int i = 0; i < cols; i++) colY[i] = top;
        int ci = 0;
        for (Category cat : Modules.CATEGORIES) {
            List<Module> mods = new ArrayList<>();
            for (Module m : cat.modules) {
                if (query.isEmpty() || m.name.toLowerCase().contains(query)) mods.add(m);
            }
            if (mods.isEmpty()) continue;
            int col = ci % cols;
            int x = x0 + col * (colW + gap);
            int y = colY[col];
            int headH = 30;
            int rowH = 22;
            int cardH = headH + mods.size() * rowH + 8;

            Draw.roundRect(g, x, y + 3, colW, cardH, 12, 0x33000000);
            Draw.roundRectBorder(g, x, y, colW, cardH, 12, t.panel, t.border());

            // шапка категории (иконка — дефолтный шрифт для глифов)
            Draw.roundRect(g, x + 12, y + 6, 18, 18, 5, t.accentSoft());
            g.drawString(this.font, cat.icon, x + 12 + (18 - this.font.width(cat.icon)) / 2, y + 6 + 5, t.accent, false);
            g.drawString(this.font, Fonts.display(cat.name), x + 36, y + 11, t.text, false);
            int on = 0;
            for (Module m : cat.modules) if (m.enabled) on++;
            Component cnt = Fonts.body(on + "/" + cat.modules.size());
            int cntW = this.font.width(cnt) + 12;
            Draw.roundRect(g, x + colW - 12 - cntW, y + 8, cntW, 14, 7, t.panel2);
            g.drawString(this.font, cnt, x + colW - 12 - cntW + 6, y + 11, t.muted, false);
            g.fill(x + 10, y + headH - 1, x + colW - 10, y + headH, t.border());

            int ry = y + headH + 4;
            for (Module m : mods) {
                int rx = x + 8, rw = colW - 16;
                boolean rowHover = mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rowH - 3;
                if (m.enabled) {
                    Draw.roundRectBorder(g, rx, ry, rw, rowH - 3, 6, t.accentSoft(), Draw.alpha(t.accent, 0x66));
                } else if (rowHover) {
                    Draw.roundRect(g, rx, ry, rw, rowH - 3, 6, t.panel2);
                }
                g.drawString(this.font, Fonts.body(m.name), rx + 8, ry + (rowH - 3) / 2 - 4, m.enabled ? t.accent : t.text, false);

                // тоггл
                int swW = 26, swH = 14, swX = rx + rw - 8 - swW, swY = ry + (rowH - 3 - swH) / 2;
                Draw.roundRect(g, swX, swY, swW, swH, swH / 2, m.enabled ? t.accent : 0xFFC9CCD3);
                int knob = m.enabled ? swX + swW - swH + 2 : swX + 2;
                Draw.roundRect(g, knob, swY + 2, swH - 4, swH - 4, (swH - 4) / 2, 0xFFFFFFFF);

                if (!m.key.isEmpty()) {
                    Component kc = Fonts.body(m.key);
                    int kw = this.font.width(kc) + 8;
                    int kx = swX - 6 - kw;
                    Draw.roundRectBorder(g, kx, ry + (rowH - 3) / 2 - 6, kw, 12, 4, t.panel2,
                            m.enabled ? Draw.alpha(t.accent, 0x66) : t.border());
                    g.drawString(this.font, kc, kx + 4, ry + (rowH - 3) / 2 - 4, m.enabled ? t.accent : t.muted, false);
                }
                entries.add(new Entry(m, rx, ry, rw, rowH - 3));
                ry += rowH;
            }
            colY[col] = y + cardH + gap;
            ci++;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        g.drawCenteredString(this.font, Fonts.body("Нажми R-Shift или Esc, чтобы закрыть"), this.width / 2, this.height - 16, 0xFFE0E0E4);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (super.mouseClicked(mx, my, btn)) return true;
        if (btn == 0) {
            for (Entry e : entries) {
                if (mx >= e.x && mx <= e.x + e.w && my >= e.y && my <= e.y + e.h) {
                    e.m.enabled = !e.m.enabled;
                    ClientState.event("toggle_module", e.m.name + " = " + (e.m.enabled ? "вкл" : "выкл"));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static final class Entry {
        final Module m;
        final int x, y, w, h;

        Entry(Module m, int x, int y, int w, int h) {
            this.m = m;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
