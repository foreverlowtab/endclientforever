package forever.end.client.ecf.screen;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Category;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Внутриигровой ClickGUI: категории, модули, переключатели, поиск. */
public class ClickGuiScreen extends Screen {
    private final Screen parent;
    private EditBox search;
    private String query = "";
    private final List<Entry> entries = new ArrayList<>();

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("ClickGUI"));
        this.parent = parent;
    }

    private Theme theme() {
        return ClientState.theme;
    }

    @Override
    protected void init() {
        search = new EditBox(this.font, this.width / 2 - 80, 20, 140, 16, Component.literal("search"));
        search.setHint(Component.literal("Поиск модуля…"));
        search.setResponder(s -> query = s.toLowerCase());
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .bounds(this.width / 2 + 66, 20, 16, 16).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0x9E000000);
        Theme t = theme();
        g.drawString(this.font, "ClickGUI", 20, 24, 0xFFFFFFFF, true);
        super.render(g, mx, my, pt);
        entries.clear();
        int x0 = 20, top = 50;
        int colW = 150, gap = 10;
        int boardW = this.width - 40;
        int cols = Math.max(1, (boardW + gap) / (colW + gap));
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
            int headerH = 16;
            int rowH = 14;
            int blockH = headerH + mods.size() * rowH + 6;
            Draw.panel(g, x, y, colW, blockH, t.panel, t.border());
            Draw.rect(g, x + 6, y + 4, 12, 12, t.accent);
            g.drawString(this.font, cat.icon, x + 9, y + 6, 0xFFFFFFFF, false);
            g.drawString(this.font, cat.name, x + 22, y + 5, t.text, false);
            int on = 0;
            for (Module m : cat.modules) if (m.enabled) on++;
            String cnt = on + "/" + cat.modules.size();
            g.drawString(this.font, cnt, x + colW - 6 - this.font.width(cnt), y + 5, t.muted, false);
            int ry = y + headerH;
            for (Module m : mods) {
                if (m.enabled) Draw.rect(g, x + 4, ry, colW - 8, rowH - 1, t.accentSoft());
                g.drawString(this.font, m.name, x + 8, ry + 3, m.enabled ? t.accent : t.text, false);
                int swW = 18, swH = 9, swX = x + colW - 8 - swW, swY = ry + 2;
                Draw.rect(g, swX, swY, swW, swH, m.enabled ? t.accent : 0xFFB9BEC7);
                int knob = m.enabled ? swX + swW - 8 : swX + 1;
                Draw.rect(g, knob, swY + 1, 7, swH - 2, 0xFFFFFFFF);
                if (!m.key.isEmpty()) {
                    int kw = this.font.width(m.key) + 6;
                    int kx = swX - 6 - kw;
                    g.drawString(this.font, m.key, kx + 3, ry + 3, t.muted, false);
                }
                entries.add(new Entry(m, x + 4, ry, colW - 8, rowH));
                ry += rowH;
            }
            colY[col] = y + blockH + gap;
            ci++;
        }
        g.drawCenteredString(this.font, "Нажми R-Shift или Esc, чтобы закрыть", this.width / 2, this.height - 14, 0xFFCCCCCC);
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
