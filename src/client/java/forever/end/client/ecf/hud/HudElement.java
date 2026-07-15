package forever.end.client.ecf.hud;

import com.mojang.blaze3d.vertex.PoseStack;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.module.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Базовый HUD-элемент: позиция хранится во фракциях экрана (устойчива к смене
 * разрешения), масштаб — множитель. Дочерние классы задают размер в layout()
 * и рисуют содержимое в draw() в локальных координатах (0,0 — левый верх).
 */
public abstract class HudElement {
    public final String id;
    public final String moduleName;
    public final String label;
    public final float defX, defY;

    public float fx, fy;      // фракции левого-верхнего угла (0..1) — источник истины
    public float scale = 1f;
    public int w = 40, h = 12; // размер содержимого без масштаба (обновляется в layout)
    public int lastX, lastY;   // последняя разрешённая позиция в пикселях (для редактора)
    protected boolean editing;

    protected HudElement(String id, String moduleName, String label, float defX, float defY) {
        this.id = id;
        this.moduleName = moduleName;
        this.label = label;
        this.defX = defX;
        this.defY = defY;
        this.fx = defX;
        this.fy = defY;
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

    public final void render(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        this.editing = editor;
        layout(mc);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int px = Math.round(fx * sw);
        int py = Math.round(fy * sh);
        px = Math.max(0, Math.min(px, Math.max(0, sw - scaledW())));
        py = Math.max(0, Math.min(py, Math.max(0, sh - scaledH())));
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

    public void moveBy(int dx, int dy, int sw, int sh) {
        int px = Math.max(0, Math.min(lastX + dx, Math.max(0, sw - scaledW())));
        int py = Math.max(0, Math.min(lastY + dy, Math.max(0, sh - scaledH())));
        fx = sw <= 0 ? 0 : (float) px / sw;
        fy = sh <= 0 ? 0 : (float) py / sh;
        lastX = px;
        lastY = py;
    }

    public void setScale(float s) {
        scale = Math.max(0.5f, Math.min(2.5f, Math.round(s * 100f) / 100f));
    }

    public void reset() {
        fx = defX;
        fy = defY;
        scale = 1f;
    }

    public boolean contains(double mxp, double myp) {
        return mxp >= lastX && mxp <= lastX + scaledW() && myp >= lastY && myp <= lastY + scaledH();
    }
}
