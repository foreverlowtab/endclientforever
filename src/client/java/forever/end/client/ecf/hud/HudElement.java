package forever.end.client.ecf.hud;

import com.mojang.blaze3d.vertex.PoseStack;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.module.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Базовый HUD-элемент. Позиция задаётся якорем (угол/край экрана)
 * и пиксельным смещением — так поля отступов остаются постоянными (как на сайте).
 * Дочерние классы задают размер в layout() и рисуют в draw() в локальных координатах (0,0 — левый верх).
 */
public abstract class HudElement {
    public enum Anchor { TL, TC, TR, ML, MC, MR, BL, BC, BR }

    public final String id;
    public final String moduleName;
    public final String label;
    public final Anchor anchor;
    public final int defOffX, defOffY;

    public int offX, offY;      // пиксельное смещение от якоря — источник истины
    public float scale = 1f;
    public int w = 40, h = 12;  // размер без масштаба (обновляется в layout)
    public int lastX, lastY;    // последняя позиция в пикселях (для редактора)
    protected boolean editing;

    protected HudElement(String id, String moduleName, String label, Anchor anchor, int defOffX, int defOffY) {
        this.id = id;
        this.moduleName = moduleName;
        this.label = label;
        this.anchor = anchor;
        this.defOffX = defOffX;
        this.defOffY = defOffY;
        this.offX = defOffX;
        this.offY = defOffY;
    }

    public Module module() { return Modules.find(moduleName); }

    public boolean enabled() {
        Module m = module();
        return m != null && m.enabled;
    }

    protected boolean opt(String n, boolean def) {
        Module m = module();
        if (m == null) return def;
        Setting s = m.find(n);
        return s instanceof Setting.Bool b ? b.value : def;
    }

    protected String optMode(String n, String def) {
        Module m = module();
        if (m == null) return def;
        Setting s = m.find(n);
        return s instanceof Setting.Mode md ? md.value() : def;
    }

    public int scaledW() { return Math.max(1, Math.round(w * scale)); }
    public int scaledH() { return Math.max(1, Math.round(h * scale)); }

    private int baseX(int sw, int ew) {
        return switch (anchor) {
            case TL, ML, BL -> 0;
            case TC, MC, BC -> (sw - ew) / 2;
            case TR, MR, BR -> sw - ew;
        };
    }

    private int baseY(int sh, int eh) {
        return switch (anchor) {
            case TL, TC, TR -> 0;
            case ML, MC, MR -> (sh - eh) / 2;
            case BL, BC, BR -> sh - eh;
        };
    }

    public final void render(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        this.editing = editor;
        layout(mc);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int ew = scaledW(), eh = scaledH();
        int px = baseX(sw, ew) + offX;
        int py = baseY(sh, eh) + offY;
        px = Math.max(0, Math.min(px, Math.max(0, sw - ew)));
        py = Math.max(0, Math.min(py, Math.max(0, sh - eh)));
        lastX = px;
        lastY = py;
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(px, py, 0);
        ps.scale(scale, scale, 1f);
        draw(g, mc, partial, editor);
        ps.popPose();
    }

    protected abstract void layout(Minecraft mc);

    protected abstract void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor);

    /** Сдвиг в экранных пикселях (из редактора). */
    public void moveBy(int dx, int dy, int sw, int sh) {
        int ew = scaledW(), eh = scaledH();
        int bx = baseX(sw, ew), by = baseY(sh, eh);
        offX += dx;
        offY += dy;
        offX = Math.max(-bx, Math.min((sw - ew) - bx, offX));
        offY = Math.max(-by, Math.min((sh - eh) - by, offY));
        lastX = bx + offX;
        lastY = by + offY;
    }

    public void setScale(float s) {
        scale = Math.max(0.5f, Math.min(2.5f, Math.round(s * 100f) / 100f));
    }

    public void reset() {
        offX = defOffX;
        offY = defOffY;
        scale = 1f;
    }

    public boolean contains(double mxp, double myp) {
        return mxp >= lastX && mxp <= lastX + scaledW() && myp >= lastY && myp <= lastY + scaledH();
    }
}
