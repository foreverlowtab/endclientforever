package forever.end.client.ecf.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
 *  - Rainbow Armor     -> rainbowArmor (мир, оболочка по форме надетой брони локального игрока)
 *  - Nametags          -> nametags     (мир, имя + HP + дистанция над игроками)
 *  - ClickGUI          -> clickGuiKeyEnabled (гейт для клавиши R-Shift)
 *  - Menu Blur         -> menuBlurActive / menuBlurAlpha (фон экранов в игре)
 */
public final class InterfaceFx {
    private InterfaceFx() {}

    private static final int OUTLINE = 0xC8000000;
    private static final int FULL_BRIGHT = 15728880; // 0xF000F0

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

        // Углы качания конечностей — как в HumanoidModel.setupAnim (cos(pos*0.6662)*амплитуда*скорость).
        float amt = Math.min(1.0f, p.walkAnimation.speed(partial));
        float c = Mth.cos(p.walkAnimation.position(partial) * 0.6662f);
        float legR = c * 1.4f * amt;   // правая нога
        float legL = -c * 1.4f * amt;  // левая нога (в противофазе)
        float armR = -c * 1.0f * amt;  // правая рука (в фазе с левой ногой)
        float armL = c * 1.0f * amt;   // левая рука

        ps.pushPose();
        ps.translate(px - cam.x, py - cam.y, pz - cam.z);
        ps.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        // 0=ботинки 1=поножи 2=нагрудник 3=шлем
        for (int slot = 0; slot < 4; slot++) {
            if (inv.getArmor(slot).isEmpty()) continue;
            armorShape(ps, buf, slot, armorColor(m, slot), legR, legL, armR, armL);
        }
        ps.popPose();
    }

    /** Оболочка по форме частей тела с анимацией ходьбы (локальные координаты, футы на y=0). */
    private static void armorShape(PoseStack ps, MultiBufferSource buf, int slot, int col,
                                   float legR, float legL, float armR, float armL) {
        switch (slot) {
            case 3 -> { // шлем — голова (куб 8×8×8), статичен
                float e = 0.07f;
                Render3D.box(ps, buf, -0.25f - e, 1.50f - e, -0.25f - e, 0.25f + e, 2.00f + e, 0.25f + e, col);
            }
            case 2 -> { // нагрудник — торс (статичен) + плечи/верх рук (качаются)
                float e = 0.05f;
                Render3D.box(ps, buf, -0.25f - e, 0.72f - e, -0.13f - e, 0.25f + e, 1.46f + e, 0.13f + e, col); // торс
                limbBox(ps, buf, -0.3125f, 1.45f, 0f, armR, -0.50f - e, 1.02f - e, -0.13f - e, -0.23f, 1.46f + e, 0.13f + e, col); // правая рука
                limbBox(ps, buf, 0.3125f, 1.45f, 0f, armL, 0.23f, 1.02f - e, -0.13f - e, 0.50f + e, 1.46f + e, 0.13f + e, col);   // левая рука
            }
            case 1 -> { // поножи — пояс (статичен) + верх обеих ног (качаются)
                float e = 0.035f;
                Render3D.box(ps, buf, -0.25f - e, 0.60f - e, -0.13f - e, 0.25f + e, 0.80f + e, 0.13f + e, col); // пояс
                limbBox(ps, buf, -0.125f, 0.75f, 0f, legR, -0.25f - e, 0.34f - e, -0.13f - e, -0.01f, 0.64f + e, 0.13f + e, col); // правая нога
                limbBox(ps, buf, 0.125f, 0.75f, 0f, legL, 0.01f, 0.34f - e, -0.13f - e, 0.25f + e, 0.64f + e, 0.13f + e, col);    // левая нога
            }
            case 0 -> { // ботинки — низ обеих ног (качаются вместе с ногами вокруг того же пивота бедра)
                float e = 0.05f;
                limbBox(ps, buf, -0.125f, 0.75f, 0f, legR, -0.25f - e, 0.00f, -0.14f - e, -0.01f, 0.30f + e, 0.13f + e, col); // правый
                limbBox(ps, buf, 0.125f, 0.75f, 0f, legL, 0.01f, 0.00f, -0.14f - e, 0.25f + e, 0.30f + e, 0.13f + e, col);    // левый
            }
            default -> { }
        }
    }

    /** Бокс, повёрнутый вокруг оси X относительно точки-пивота сустава (для качания конечностей). */
    private static void limbBox(PoseStack ps, MultiBufferSource buf,
                                float pvx, float pvy, float pvz, float xRot,
                                float x0, float y0, float z0, float x1, float y1, float z1, int col) {
        ps.pushPose();
        ps.translate(pvx, pvy, pvz);
        ps.mulPose(Axis.XP.rotation(xRot));
        ps.translate(-pvx, -pvy, -pvz);
        Render3D.box(ps, buf, x0, y0, z0, x1, y1, z1, col);
        ps.popPose();
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
        Font font = mc.font;
        int accent = Colors.themeAccent();

        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player && !self) continue;
            if (p.isInvisible()) continue;
            float dist = p.distanceTo(mc.player);
            if (dist > radius) continue;

            // Та же интерполяция, что и у ванильного рендерера сущностей.
            double x = Mth.lerp(partial, p.xOld, p.getX());
            double y = Mth.lerp(partial, p.yOld, p.getY()) + p.getBbHeight() + 0.6;
            double z = Mth.lerp(partial, p.zOld, p.getZ());

            ps.pushPose();
            ps.translate(x - cam.x, y - cam.y, z - cam.z);
            ps.mulPose(orient);
            float s = 0.025f * scale;
            ps.scale(s, -s, s); // X положительный — текст не зеркалится
            Matrix4f mtx = ps.last().pose();

            String name = p.getName().getString();
            int hp = (int) Math.ceil(p.getHealth());
            int maxHp = (int) Math.ceil(p.getMaxHealth());
            float frac = Math.max(0f, Math.min(1f, p.getHealth() / Math.max(1f, p.getMaxHealth())));
            int hpCol = frac > 0.5f ? 0xFF5BD16A : (frac > 0.25f ? 0xFFE7B008 : 0xFFE5484D);
            String info = hp + "/" + maxHp + " HP   " + String.format(Locale.US, "%.1f", dist) + " м";

            int bg = 0x80000000;
            // Имя (белым) и строка HP+дистанция (цвет по HP). SEE_THROUGH — читается сквозь препятствия.
            font.drawInBatch(name, -font.width(name) / 2f, -20f, 0xFFFFFFFF, true, mtx, buf, Font.DisplayMode.SEE_THROUGH, bg, FULL_BRIGHT);
            font.drawInBatch(info, -font.width(info) / 2f, -9f, hpCol, true, mtx, buf, Font.DisplayMode.SEE_THROUGH, bg, FULL_BRIGHT);

            // акцентная подчёркивающая линия под текстом
            Render3D.quad(ps, buf, -24f, 1f, 0f, 24f, 1f, 0f, 24f, 2f, 0f, -24f, 2f, 0f, accent);

            if (showHp) {
                float bw = 90f;
                float bx0 = -bw / 2f, bx1 = bw / 2f;
                Render3D.quad(ps, buf, bx0, 4f, 0f, bx1, 4f, 0f, bx1, 8f, 0f, bx0, 8f, 0f, 0xC01A1E24); // трек
                float fx = bx0 + bw * frac;
                Render3D.quad(ps, buf, bx0, 4f, 0f, fx, 4f, 0f, fx, 8f, 0f, bx0, 8f, 0f, hpCol);        // заполнение
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
