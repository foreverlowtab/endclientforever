package forever.end.client.ecf.screen;

import java.util.List;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.EcfConfig;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Редактор HUD: перетаскивание, масштаб (колесо/скобки), сетка, сброс. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudElement selected;
    private boolean dragging;
    private double dragOffX, dragOffY;
    private static boolean grid = true;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    private Theme theme() { return ClientState.theme; }

    @Override
    protected void init() {
        int bw = 128, bh = 20, gap = 8;
        int total = bw * 3 + gap * 2;
        int x = (this.width - total) / 2;
        int y = this.height - 30;
        addRenderableWidget(new UiButton(x, y, bw, bh, Fonts.body("\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0432\u0441\u0451"),
                UiButton.Style.GHOST, this::resetAll));
        addRenderableWidget(new UiButton(x + bw + gap, y, bw, bh,
                Fonts.body("\u0421\u0435\u0442\u043a\u0430: " + (grid ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b")),
                UiButton.Style.GHOST, () -> { grid = !grid; this.rebuildWidgets(); }));
        addRenderableWidget(new UiButton(x + 2 * (bw + gap), y, bw, bh, Fonts.body("\u0413\u043e\u0442\u043e\u0432\u043e"),
                UiButton.Style.PRIMARY, this::onClose));
    }

    private void resetAll() {
        for (HudElement e : HudManager.elements()) e.reset();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, this.width, this.height, 0xC00A0B0D, 0xD8060708);
        if (grid) {
            int c = 0x12FFFFFF;
            for (int gx = 0; gx < this.width; gx += 12) g.fill(gx, 0, gx + 1, this.height, c);
            for (int gy = 0; gy < this.height; gy += 12) g.fill(0, gy, this.width, gy + 1, c);
        }
        List<HudElement> els = HudManager.elements();
        for (HudElement e : els) {
            boolean on = e.enabled();
            e.render(g, this.minecraft, pt, true);
            // Затемняем выключенные элементы полупрозрачной накладкой.
            if (!on) g.fill(e.lastX - 2, e.lastY - 2, e.lastX + e.scaledW() + 2, e.lastY + e.scaledH() + 2, 0x990B0D10);
            int col = e == selected ? theme().accent : 0x55FFFFFF;
            Draw.border(g, e.lastX - 2, e.lastY - 2, e.scaledW() + 4, e.scaledH() + 4, col);
            if (e == selected) {
                Draw.roundRect(g, e.lastX + e.scaledW() - 2, e.lastY + e.scaledH() - 2, 5, 5, 2, theme().accent);
            }
            Component lb = Fonts.body(e.label + (on ? "" : " (\u0432\u044b\u043a\u043b)"));
            g.drawString(this.font, lb, e.lastX - 2, e.lastY - 11, on ? 0xFFFFFFFF : 0xFF9AA0AC, true);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int acc = theme().accent;
        Draw.roundRect(g, 12, 10, 18, 18, 5, acc);
        g.drawString(this.font, Fonts.display("\u25a6"), 16, 15, 0xFFFFFFFF, false);
        g.drawString(this.font, Fonts.display("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 HUD"), 36, 11, 0xFFFFFFFF, true);
        g.drawString(this.font, Fonts.body(
                "\u041f\u0435\u0440\u0435\u0442\u0430\u0449\u0438 \u043c\u044b\u0448\u044c\u044e \u00b7 \u043a\u043e\u043b\u0435\u0441\u043e \u2014 \u0440\u0430\u0437\u043c\u0435\u0440 \u00b7 \u0441\u0442\u0440\u0435\u043b\u043a\u0438 \u2014 \u0442\u043e\u0447\u043d\u043e (Shift \u00d710) \u00b7 [ ] \u2014 \u043c\u0430\u0441\u0448\u0442\u0430\u0431 \u00b7 R \u2014 \u0441\u0431\u0440\u043e\u0441 \u00b7 \u041f\u041a\u041c \u2014 \u0432\u043a\u043b/\u0432\u044b\u043a\u043b"),
                36, 22, 0xFFB9BEC9, false);
        if (selected != null) {
            String s = selected.label + "  \u00b7  " + Math.round(selected.scale * 100) + "%";
            Component info = Fonts.body(s);
            int iw = this.font.width(info) + 16;
            Draw.roundRect(g, this.width - iw - 12, 12, iw, 16, 8, acc);
            g.drawString(this.font, info, this.width - iw - 12 + 8, 16, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (super.mouseClicked(mx, my, btn)) return true;
        List<HudElement> els = HudManager.elements();
        for (int i = els.size() - 1; i >= 0; i--) {
            HudElement e = els.get(i);
            if (e.contains(mx, my)) {
                selected = e;
                if (btn == 1) {
                    Module m = e.module();
                    if (m != null) {
                        m.toggle();
                        ClientState.event("toggle_module", m.name + " = " + (m.enabled ? "\u0432\u043a\u043b" : "\u0432\u044b\u043a\u043b"));
                    }
                    return true;
                }
                if (btn == 0) {
                    dragging = true;
                    dragOffX = mx - e.lastX;
                    dragOffY = my - e.lastY;
                }
                return true;
            }
        }
        selected = null;
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging && selected != null && btn == 0) {
            int targetX = (int) Math.round(mx - dragOffX);
            int targetY = (int) Math.round(my - dragOffY);
            selected.moveBy(targetX - selected.lastX, targetY - selected.lastY, this.width, this.height);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dxs, double dys) {
        List<HudElement> els = HudManager.elements();
        for (int i = els.size() - 1; i >= 0; i--) {
            HudElement e = els.get(i);
            if (e.contains(mx, my)) {
                selected = e;
                e.setScale(e.scale + (float) dys * 0.05f);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, dxs, dys);
    }

    @Override
    public boolean keyPressed(int k, int sc, int mod) {
        if (k == GLFW.GLFW_KEY_ESCAPE || k == GLFW.GLFW_KEY_ENTER) {
            onClose();
            return true;
        }
        if (selected != null) {
            int step = (mod & GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;
            switch (k) {
                case GLFW.GLFW_KEY_LEFT -> { selected.moveBy(-step, 0, this.width, this.height); return true; }
                case GLFW.GLFW_KEY_RIGHT -> { selected.moveBy(step, 0, this.width, this.height); return true; }
                case GLFW.GLFW_KEY_UP -> { selected.moveBy(0, -step, this.width, this.height); return true; }
                case GLFW.GLFW_KEY_DOWN -> { selected.moveBy(0, step, this.width, this.height); return true; }
                case GLFW.GLFW_KEY_LEFT_BRACKET -> { selected.setScale(selected.scale - 0.05f); return true; }
                case GLFW.GLFW_KEY_RIGHT_BRACKET -> { selected.setScale(selected.scale + 0.05f); return true; }
                case GLFW.GLFW_KEY_R -> { selected.reset(); return true; }
                default -> { }
            }
        }
        return super.keyPressed(k, sc, mod);
    }

    @Override
    public void onClose() {
        EcfConfig.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
