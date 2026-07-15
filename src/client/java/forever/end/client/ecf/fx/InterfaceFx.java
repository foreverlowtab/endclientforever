package forever.end.client.ecf.fx;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Реализация категории Interface.
 *
 * Всё сделано на тех же API, что и Cosmetics/Effects (Fabric events + Render3D/Draw/Colors),
 * без новых миксинов, чтобы гарантированно собираться и не падать на запуске.
 *
 *  - Chroma           -> глобальная радуга акцентов (см. Colors.chroma / themeAccent)
 *  - Custom Crosshair  -> crosshairHud (HUD)
 *  - Glint Colorizer   -> glintHud     (HUD, подсветка зачарованных предметов в хотбаре)
 *  - Rainbow Armor     -> rainbowArmor (мир, оболочка поверх надетой брони локального игрока)
 *  - Nametags          -> nametags     (мир, полоса здоровья над игроками)
 *  - ClickGUI          -> clickGuiKeyEnabled (гейт для клавиши R-Shift)
 *  - Menu Blur         -> menuBlurActive / menuBlurAlpha (фон экранов в игре)
 */
public final class InterfaceFx {
    private InterfaceFx() {}

    private static final int OUTLINE = 0xC8000000;

    // ==================== Custom Crosshair ====================
    public static void crosshairHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.screen != null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        int size = Math.max(1, m.inti("Размер"));
        int gap = Math.max(0, m.inti("Зазор"));
        int t = Math.max(1, m.inti("Толщина"));
        int col = m.color("Цвет");
        boolean outline = m.bool("Контур");
        String style = m.mode("Стиль");

        boolean circle = "Круг".equals(style);
        boolean dotOnly = "Точка".equals(style);
        boolean arms = !dotOnly && !circle;
        boolean withUp = !"Т-образный".equals(style);
        boolean dot = dotOnly || "Крест+точка".equals(style);

        if (circle) ring(g, cx, cy, size + gap, t, col, outline);

        if (arms) {
            List<int[]> rects = new ArrayList<>();
            int by = cy - t / 2;
            int bx = cx - t / 2;
            rects.add(new int[]{cx - gap - size, by, size, t}); // лево
            rects.add(new int[]{cx + gap, by, size, t});        // право
            rects.add(new int[]{bx, cy + gap, t, size});        // низ
            if (withUp) rects.add(new int[]{bx, cy - gap - size, t, size}); // верх
            if (outline) for (int[] r : rects) Draw.rect(g, r[0] - 1, r[1] - 1, r[2] + 2, r[3] + 2, OUTLINE);
            for (int[] r : rects) Draw.rect(g, r[0], r[1], r[2], r[3], col);
        }

        if (dot) {
            int ds = dotOnly ? Math.max(2, Math.round(size * 0.7f)) : Math.max(2, t + 1);
            int dx = cx - ds / 2, dy = cy - ds / 2;
            if (outline) Draw.rect(g, dx - 1, dy - 1, ds + 2, ds + 2, OUTLINE);
            Draw.rect(g, dx, dy, ds, ds, col);
        }
    }

    private static void ring(GuiGraphics g, int cx, int cy, int r, int t, int col, boolean outline) {
        int th = Math.max(1, t);
        for (int a = 0; a < 360; a += 7) {
            double rad = Math.toRadians(a);
            int px = cx + (int) Math.round(Math.cos(rad) * r);
            int py = cy + (int) Math.round(Math.sin(rad) * r);
            if (outline) Draw.rect(g, px - th / 2 - 1, py - th / 2 - 1, th + 2, th + 2, OUTLINE);
            Draw.rect(g, px - th / 2, py - th / 2, th, th, col);
        }
    }

    // ==================== Glint Colorizer ====================
    public static void glintHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int sw = g.guiWidth();
        int sh = g.guiHeight();
        int x0 = sw / 2 - 91;
        int y0 = sh - 22;
        int alpha = (int) Math.round(Math.max(10, m.num("Интенсивность")) / 100.0 * 255.0);
        String mode = m.mode("Режим");
        float speed = Math.max(0.05f, m.numf("Скорость"));
        long period = (long) (3000f / speed);
        if (period < 200L) period = 200L;
        float base = (System.currentTimeMillis() % period) / (float) period;

        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !s.hasFoil()) continue;
            int sx = x0 + 3 + i * 20;
            int sy = y0 + 3;
            int c1, c2;
            if ("Акцент".equals(mode)) {
                c1 = Colors.withAlpha(Colors.themeAccent(), alpha);
                c2 = Colors.withAlpha(Colors.themeAccent2(), alpha);
            } else if ("Свой".equals(mode)) {
                int cc = m.color("Цвет");
                c1 = Colors.withAlpha(cc, alpha);
                c2 = Colors.withAlpha(Render3D.shade(cc, 1.4f), alpha);
            } else {
                float h1 = (base + i * 0.06f) % 1f;
                float h2 = (h1 + 0.12f) % 1f;
                c1 = Colors.withAlpha(Colors.fromHue(h1), alpha);
                c2 = Colors.withAlpha(Colors.fromHue(h2), alpha);
            }
            g.fillGradient(sx, sy, sx + 16, sy + 16, c1, c2);
        }
    }

    // ==================== Rainbow Armor ====================
    public static void rainbowArmor(WorldRenderContext ctx, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        AbstractClientPlayer p = mc.player;
        if (p == null || p.isInvisible()) return;

        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 cam = ctx.camera().getPosition();
        double px = Mth.lerp(partial, p.xOld, p.getX());
        double py = Mth.lerp(partial, p.yOld, p.getY());
        double pz = Mth.lerp(partial, p.zOld, p.getZ());
        float bodyYaw = Mth.lerp(partial, p.yBodyRotO, p.yBodyRot);
        var inv = p.getInventory();

        ps.pushPose();
        ps.translate(px - cam.x, py - cam.y, pz - cam.z);
        ps.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        for (int i = 0; i < 4; i++) {
            if (inv.getArmor(i).isEmpty()) continue; // 0=ботинки 1=штаны 2=нагрудник 3=шлем
            shell(ps, buf, i, armorColor(m, i));
        }
        ps.popPose();
    }

    private static void shell(PoseStack ps, MultiBufferSource buf, int part, int col) {
        float e = 0.04f;
        switch (part) {
            case 0 -> Render3D.box(ps, buf, -0.24f - e, 0.00f - e, -0.16f - e, 0.24f + e, 0.28f + e, 0.16f + e, col);
            case 1 -> Render3D.box(ps, buf, -0.24f - e, 0.28f - e, -0.16f - e, 0.24f + e, 0.72f + e, 0.16f + e, col);
            case 2 -> Render3D.box(ps, buf, -0.26f - e, 0.72f - e, -0.18f - e, 0.26f + e, 1.38f + e, 0.18f + e, col);
            case 3 -> Render3D.box(ps, buf, -0.20f - e, 1.40f - e, -0.20f - e, 0.20f + e, 1.78f + e, 0.20f + e, col);
            default -> { }
        }
    }

    private static int armorColor(Module m, int part) {
        int alpha = (int) Math.round(Math.max(10, m.num("Прозрачность")) / 100.0 * 255.0);
        if ("Акцент".equals(m.mode("Режим"))) {
            return Colors.withAlpha(Colors.themeAccent(), alpha);
        }
        float speed = Math.max(0.05f, m.numf("Скорость"));
        long period = (long) (4000f / speed);
        if (period < 200L) period = 200L;
        float base = (System.currentTimeMillis() % period) / (float) period;
        float hue = (base + part * 0.12f) % 1f;
        return Colors.withAlpha(Colors.fromHue(hue), alpha);
    }

    // ==================== Nametags ====================
    public static void nametags(WorldRenderContext ctx, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;

        boolean showHp = m.bool("Здоровье");
        boolean self = m.bool("Свои");
        double radius = Math.max(4.0, m.num("Радиус"));
        float scale = m.numf("Масштаб");
        if (scale <= 0f) scale = 1f;
        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 cam = ctx.camera().getPosition();
        Quaternionf orient = mc.getEntityRenderDispatcher().cameraOrientation();
        int accent = Colors.themeAccent();

        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player && !self) continue;
            if (p.isInvisible()) continue;
            if (p.distanceTo(mc.player) > radius) continue;

            double x = Mth.lerp(partial, p.xOld, p.getX());
            double y = Mth.lerp(partial, p.yOld, p.getY()) + p.getBbHeight() + 0.55;
            double z = Mth.lerp(partial, p.zOld, p.getZ());

            ps.pushPose();
            ps.translate(x - cam.x, y - cam.y, z - cam.z);
            ps.mulPose(orient);
            float s = 0.025f * scale;
            ps.scale(-s, -s, s);

            // подложка + верхняя акцентная линия
            Render3D.quad(ps, buf, -24, -2, 0, 24, -2, 0, 24, 9, 0, -24, 9, 0, 0xC00A0C12);
            Render3D.quad(ps, buf, -24, -2, 0, 24, -2, 0, 24, -1, 0, -24, -1, 0, accent);

            if (showHp) {
                float frac = Math.max(0f, Math.min(1f, p.getHealth() / Math.max(1f, p.getMaxHealth())));
                Render3D.quad(ps, buf, -20, 3, 0, 20, 3, 0, 20, 6, 0, -20, 6, 0, 0xFF20242C);
                int hc = frac > 0.5f ? 0xFF46A171 : (frac > 0.25f ? accent : 0xFFE5484D);
                float fw = 40f * frac;
                Render3D.quad(ps, buf, -20, 3, 0, -20 + fw, 3, 0, -20 + fw, 6, 0, -20, 6, 0, hc);
            }
            ps.popPose();
        }
    }

    // ==================== ClickGUI / Menu Blur (гейты) ====================
    /** Разрешено ли открывать ClickGUI по R-Shift. */
    public static boolean clickGuiKeyEnabled() {
        Module m = Modules.find("ClickGUI");
        return m == null || (m.enabled && m.bool("R-Shift"));
    }

    /** Активен ли Menu Blur для внутриигровых экранов. */
    public static boolean menuBlurActive() {
        Module m = Modules.find("Menu Blur");
        return m != null && m.enabled && m.bool("В игре");
    }

    /** Альфа фона (0..255) для Menu Blur. */
    public static int menuBlurAlpha() {
        Module m = Modules.find("Menu Blur");
        double v = (m == null) ? 55 : Math.max(10, m.num("Интенсивность"));
        return (int) Math.round(v / 100.0 * 255.0);
    }
}
