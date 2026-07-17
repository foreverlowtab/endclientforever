package forever.end.client.ecf.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Реализация категории Animations.
 *
 * Как и Cosmetics/Effects/Interface, всё построено на публичных Fabric-событиях
 * (WorldRenderContext / HUD / client tick) + Render3D/Colors, без новых миксинов —
 * чтобы гарантированно собираться и не падать на запуске.
 *
 *  - Old Animations   -> oldSwingRender  (мир: дуга/клинок/круг взмаха по getAttackAnim)
 *  - 3D Skin Layers    -> skinLayers      (мир: объёмные слои-оболочки на теле игрока)
 *  - Emotes            -> emoteTick + emoteRender (клавиша B проигрывает эмоцию; 4 стиля)
 *  - Custom Rotations  -> rotationTick    (управление разворотом корпуса игрока)
 *  - Sprint FX         -> sprintTick + sprintHud (эффект скорости при спринте; 3 стиля)
 *  - Item Physics      -> itemPhysics     (мир: "осевшие" выпавшие предметы рядом)
 *
 * Примечание: более глубокие версии Old Animations (тайминг ванильного взмаха)
 * и Item Physics (плоская укладка предметов) потребуют миксинов в
 * ItemInHandRenderer / ItemEntityRenderer — это опциональная доработка.
 */
public final class AnimationFx {
    private AnimationFx() {}

    private interface Body {
        void draw(PoseStack ps, MultiBufferSource buf, AbstractClientPlayer p, float partial);
    }

    private static float partial(WorldRenderContext ctx) {
        return ctx.tickCounter().getGameTimeDeltaPartialTick(false);
    }

    /** Поза в центре локального игрока (ноги = 0), опционально повёрнутая по корпусу. */
    private static void atPlayer(WorldRenderContext ctx, boolean rotateBody, Body fn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        AbstractClientPlayer p = mc.player;
        if (p == null) return;
        float pt = partial(ctx);
        Vec3 cam = ctx.camera().getPosition();
        double px = Mth.lerp(pt, p.xOld, p.getX());
        double py = Mth.lerp(pt, p.yOld, p.getY());
        double pz = Mth.lerp(pt, p.zOld, p.getZ());
        float bodyYaw = Mth.lerp(pt, p.yBodyRotO, p.yBodyRot);
        ps.pushPose();
        ps.translate(px - cam.x, py - cam.y, pz - cam.z);
        if (rotateBody) ps.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        fn.draw(ps, buf, p, pt);
        ps.popPose();
    }

    /** Текст-билборд в текущей локальной точке (смещённый вверх на yOff). */
    private static void billboardLocal(PoseStack ps, MultiBufferSource buf, float yOff, String text, int col) {
        Minecraft mc = Minecraft.getInstance();
        Quaternionf orient = mc.getEntityRenderDispatcher().cameraOrientation();
        ps.pushPose();
        ps.translate(0f, yOff, 0f);
        ps.mulPose(orient);
        float s = 0.02f;
        ps.scale(s, -s, s);
        Matrix4f mtx = ps.last().pose();
        Font font = mc.font;
        font.drawInBatch(text, -font.width(text) / 2f, 0f, col, false, mtx, buf, Font.DisplayMode.NORMAL, 0, 15728880);
        ps.popPose();
    }

    private static void dust(Level level, double x, double y, double z, int color, int count, double spread, double speed) {
        if (level == null) return;
        DustParticleOptions opt = new DustParticleOptions(color & 0xFFFFFF, 1.0f);
        for (int i = 0; i < count; i++) {
            level.addParticle(opt,
                    x + (Math.random() - 0.5) * spread,
                    y + (Math.random() - 0.5) * spread,
                    z + (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * speed, Math.random() * speed, (Math.random() - 0.5) * speed);
        }
    }

    // ==================== Old Animations (дуга взмаха) ====================
    public static void oldSwingRender(WorldRenderContext ctx, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float pt = partial(ctx);
        float swing = mc.player.getAttackAnim(pt);
        if (swing <= 0.02f) return;
        int col = m.color("Цвет");
        float len = m.numf("Длина");
        String style = m.mode("Стиль");
        atPlayer(ctx, true, (ps, buf, p, ppt) -> {
            float rx = -0.15f;      // правая рука (сторона -X)
            float pivotY = 1.35f;   // высота плеча
            float rIn = 0.30f * len, rOut = 0.95f * len;
            if ("Круг".equals(style)) {
                float rr = (0.40f + swing * 0.50f) * len;
                int a = (int) (200 * swing);
                ps.pushPose();
                ps.translate(rx, pivotY, 0.35f);
                Render3D.ringXZ(ps, buf, 0f, rr * 0.7f, rr, 28, Colors.withAlpha(col, a));
                ps.popPose();
                return;
            }
            if ("Клинок".equals(style)) {
                float th = (float) Math.toRadians(60 - swing * 130);
                float y0 = pivotY + (float) Math.sin(th) * rIn, z0 = (float) Math.cos(th) * rIn;
                float y1 = pivotY + (float) Math.sin(th) * rOut, z1 = (float) Math.cos(th) * rOut;
                int a = (int) (220 * swing);
                Render3D.quad(ps, buf, rx - 0.03f, y0, z0, rx - 0.03f, y1, z1,
                        rx + 0.03f, y1, z1, rx + 0.03f, y0, z0, Colors.withAlpha(col, a));
                return;
            }
            // "Дуга" — веер затухающих сегментов в вертикальной плоскости перед рукой.
            int seg = 8;
            float lead = 60f - swing * 130f;
            float span = 80f;
            for (int i = 0; i < seg; i++) {
                float t0 = (float) Math.toRadians(lead + span * i / seg);
                float t1 = (float) Math.toRadians(lead + span * (i + 1) / seg);
                int a = (int) (200 * swing * (1f - i / (float) seg));
                int c = Colors.withAlpha(col, Math.max(0, a));
                float y0 = pivotY + (float) Math.sin(t0) * rIn, z0 = (float) Math.cos(t0) * rIn;
                float y1 = pivotY + (float) Math.sin(t0) * rOut, z1 = (float) Math.cos(t0) * rOut;
                float y2 = pivotY + (float) Math.sin(t1) * rOut, z2 = (float) Math.cos(t1) * rOut;
                float y3 = pivotY + (float) Math.sin(t1) * rIn, z3 = (float) Math.cos(t1) * rIn;
                Render3D.quad(ps, buf, rx, y0, z0, rx, y1, z1, rx, y2, z2, rx, y3, z3, c);
            }
        });
    }

    // ==================== 3D Skin Layers ====================
    public static void skinLayers(WorldRenderContext ctx, Module m) {
        atPlayer(ctx, true, (ps, buf, p, pt) -> {
            if (p.isInvisible()) return;
            String scope = m.mode("Слои");
            float e = Math.max(0.01f, m.numf("Толщина"));
            int alpha = (int) Math.round(Math.max(10, m.num("Прозрачность")) / 100.0 * 255.0);
            int col = Colors.withAlpha(m.color("Цвет"), alpha);

            float amt = Math.min(1.0f, p.walkAnimation.speed(pt));
            float c = Mth.cos(p.walkAnimation.position(pt) * 0.6662f);
            float legR = c * 1.4f * amt, legL = -c * 1.4f * amt;
            float armR = -c * 1.0f * amt, armL = c * 1.0f * amt;

            // Голова (второй слой всегда)
            Render3D.box(ps, buf, -0.25f - e, 1.50f - e, -0.25f - e, 0.25f + e, 2.00f + e, 0.25f + e, col);

            if (!"Голова".equals(scope)) {
                // Торс + верх рук
                Render3D.box(ps, buf, -0.25f - e, 0.72f - e, -0.13f - e, 0.25f + e, 1.46f + e, 0.13f + e, col);
                limbBox(ps, buf, -0.3125f, 1.45f, 0f, armR, -0.50f - e, 1.02f - e, -0.13f - e, -0.23f, 1.46f + e, 0.13f + e, col);
                limbBox(ps, buf, 0.3125f, 1.45f, 0f, armL, 0.23f, 1.02f - e, -0.13f - e, 0.50f + e, 1.46f + e, 0.13f + e, col);
            }
            if ("Полностью".equals(scope)) {
                // Ноги (качаются вокруг бедра)
                limbBox(ps, buf, -0.125f, 0.75f, 0f, legR, -0.25f - e, 0.00f, -0.13f - e, -0.01f, 0.64f + e, 0.13f + e, col);
                limbBox(ps, buf, 0.125f, 0.75f, 0f, legL, 0.01f, 0.00f, -0.13f - e, 0.25f + e, 0.64f + e, 0.13f + e, col);
            }
        });
    }

    /** Бокс, повёрнутый вокруг оси X относительно точки-пивота сустава. */
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

    // ==================== Emotes ====================
    private static boolean bPrev = false;
    private static long emoteStart = 0L;
    private static long emoteUntil = 0L;
    private static String emoteStyle = "";

    public static void emoteTick(Minecraft mc, Module m) {
        if (mc.player == null) return;
        boolean down = mc.screen == null && mc.getWindow() != null
                && GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS;
        if (down && !bPrev) {
            emoteStyle = m.mode("Эмоция");
            float speed = Math.max(0.3f, m.numf("Скорость"));
            emoteStart = System.currentTimeMillis();
            emoteUntil = emoteStart + (long) (1800f / speed);
        }
        bPrev = down;
    }

    public static void emoteRender(WorldRenderContext ctx, Module m) {
        long now = System.currentTimeMillis();
        if (emoteStart == 0L || now >= emoteUntil) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float dur = Math.max(1f, emoteUntil - emoteStart);
        float prog = Math.min(1f, (now - emoteStart) / dur);
        int col = m.color("Цвет");
        String style = emoteStyle.isEmpty() ? m.mode("Эмоция") : emoteStyle;
        atPlayer(ctx, true, (ps, buf, p, pt) -> {
            float head = p.getBbHeight();
            switch (style) {
                case "Сердце" -> emoteHeart(ps, buf, head, prog, col);
                case "Салют" -> emoteFirework(ps, buf, head, prog, col);
                case "Спираль" -> emoteSpiral(ps, buf, head, prog, col);
                default -> emoteWave(ps, buf, head, prog, col);
            }
        });
        if (m.bool("Подпись")) {
            String label = switch (style) {
                case "Сердце" -> "<3";
                case "Салют" -> "\\o/";
                case "Спираль" -> "~";
                default -> "Привет!";
            };
            atPlayer(ctx, false, (ps, buf, p, pt) ->
                    billboardLocal(ps, buf, p.getBbHeight() + 1.05f, label, Colors.withAlpha(0xFFFFFFFF, 255)));
        }
    }

    private static void emoteWave(PoseStack ps, MultiBufferSource buf, float head, float prog, int col) {
        float swing = (float) Math.sin(prog * Math.PI * 6) * 40f;
        ps.pushPose();
        ps.translate(0.42f, head - 0.25f, 0.05f);
        ps.mulPose(Axis.ZP.rotationDegrees(swing));
        Render3D.box(ps, buf, -0.05f, 0f, -0.05f, 0.05f, 0.32f, 0.05f, col);
        Render3D.box(ps, buf, -0.07f, 0.30f, -0.07f, 0.07f, 0.44f, 0.07f, Render3D.shade(col, 1.15f));
        ps.popPose();
    }

    private static void emoteHeart(PoseStack ps, MultiBufferSource buf, float head, float prog, int col) {
        float rise = prog * 0.5f;
        float pulse = 1f + 0.12f * (float) Math.sin(System.currentTimeMillis() / 120.0);
        float s = 0.09f;
        ps.pushPose();
        ps.translate(0f, head + 0.5f + rise, 0f);
        ps.scale(pulse, pulse, pulse);
        Render3D.box(ps, buf, -2 * s, 0f, -s, 0f, 2 * s, s, col);
        Render3D.box(ps, buf, 0f, 0f, -s, 2 * s, 2 * s, s, col);
        ps.mulPose(Axis.ZP.rotationDegrees(45));
        Render3D.box(ps, buf, -1.4f * s, -1.4f * s, -s, 1.4f * s, 1.4f * s, s, col);
        ps.popPose();
    }

    private static void emoteFirework(PoseStack ps, MultiBufferSource buf, float head, float prog, int col) {
        float r = 0.15f + prog * 1.1f;
        int a = (int) (220 * (1f - prog));
        int c = Colors.withAlpha(col, Math.max(0, a));
        int white = Colors.withAlpha(0xFFFFFFFF, Math.max(0, (int) (a * 0.8f)));
        ps.pushPose();
        ps.translate(0f, head + 0.8f, 0f);
        Render3D.ringXZ(ps, buf, 0f, r * 0.85f, r, 40, c);
        Render3D.ringXZ(ps, buf, 0.2f, r * 0.6f, r * 0.72f, 40, white);
        int spikes = 10;
        for (int i = 0; i < spikes; i++) {
            double ang = Math.PI * 2 * i / spikes;
            float dx = (float) Math.cos(ang) * r, dz = (float) Math.sin(ang) * r;
            Render3D.box(ps, buf, dx - 0.02f, -0.02f, dz - 0.02f, dx + 0.02f, 0.02f + r * 0.3f, dz + 0.02f, c);
        }
        ps.popPose();
    }

    private static void emoteSpiral(PoseStack ps, MultiBufferSource buf, float head, float prog, int col) {
        int n = 14;
        float t = (float) (System.currentTimeMillis() / 6.0);
        ps.pushPose();
        ps.translate(0f, head + 0.2f, 0f);
        for (int i = 0; i < n; i++) {
            float f = i / (float) n;
            float y = f * (0.3f + prog * 1.2f);
            float ang = (float) Math.toRadians(i * 45 + t);
            float rad = 0.28f * (1f - f * 0.4f);
            float dx = (float) Math.cos(ang) * rad, dz = (float) Math.sin(ang) * rad;
            int c = Colors.withAlpha(col, (int) (200 * (1f - f)));
            ps.pushPose();
            ps.translate(dx, y, dz);
            Render3D.discXZ(ps, buf, 0f, 0.06f, 10, c);
            ps.popPose();
        }
        ps.popPose();
    }

    // ==================== Custom Rotations ====================
    private static boolean rotLocked = false;
    private static float rotYaw = 0f;

    public static void rotationTick(Minecraft mc, Module m) {
        AbstractClientPlayer p = mc.player;
        if (p == null) return;
        String mode = m.mode("Тело");
        if ("Вперёд по камере".equals(mode)) {
            p.yBodyRot = p.getYRot();
            p.yBodyRotO = p.getYRot();
            rotLocked = false;
        } else if ("Статичное".equals(mode)) {
            if (!rotLocked) { rotYaw = p.yBodyRot; rotLocked = true; }
            p.yBodyRot = rotYaw;
            p.yBodyRotO = rotYaw;
        } else {
            rotLocked = false;
        }
    }

    public static void rotationReset(Module m) {
        rotLocked = false;
    }

    // ==================== Sprint FX ====================
    private static float sprintAmt = 0f;

    public static void sprintTick(Minecraft mc, Module m) {
        AbstractClientPlayer p = mc.player;
        if (p == null) { sprintAmt *= 0.8f; return; }
        double sp = Math.hypot(p.getX() - p.xOld, p.getZ() - p.zOld);
        boolean active = p.isSprinting() && sp > 0.03;
        sprintAmt += ((active ? 1f : 0f) - sprintAmt) * 0.25f;
        if (active && "Частицы".equals(m.mode("Стиль")) && mc.level != null) {
            double rad = Math.toRadians(p.getYRot());
            double bx = Math.sin(rad), bz = -Math.cos(rad); // позади игрока
            dust(mc.level, p.getX() + bx * 0.25, p.getY() + 0.15, p.getZ() + bz * 0.25, m.color("Цвет"), 2, 0.15, 0.05);
        }
    }

    public static void sprintHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null) return;
        float amt = sprintAmt;
        if (amt < 0.03f) return;
        String style = m.mode("Стиль");
        if ("Частицы".equals(style)) return; // обрабатывается частицами в мире
        int col = m.color("Цвет");
        int intensity = Math.max(10, m.inti("Интенсивность"));
        int w = g.guiWidth(), h = g.guiHeight();
        if ("Туннель".equals(style)) {
            int a = (int) (intensity / 100f * 150f * amt);
            int c = Colors.withAlpha(col, a);
            int trans = col & 0x00FFFFFF;
            int band = Math.max(30, w / 4);
            g.fillGradient(0, 0, band, h, c, trans);
            g.fillGradient(w - band, 0, w, h, trans, c);
            int vb = Math.max(20, h / 5);
            g.fillGradient(0, 0, w, vb, c, trans);
            g.fillGradient(0, h - vb, w, h, trans, c);
        } else { // "Линии"
            int c = Colors.withAlpha(col, (int) (190 * amt));
            int n = 4 + intensity / 12;
            long t = System.currentTimeMillis();
            int len = (int) ((30 + intensity) * amt);
            for (int i = 0; i < n; i++) {
                int yy = (int) (h * (i + 0.5f) / n);
                int span = Math.max(60, w / 2);
                int lx = (int) ((t / 4 + i * 140) % span);
                g.fill(lx, yy, lx + len, yy + 2, c);
                int rx = w - lx;
                g.fill(rx - len, yy + 6, rx, yy + 8, c);
            }
        }
    }

    // ==================== Item Physics ====================
    public static void itemPhysics(WorldRenderContext ctx, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        float pt = partial(ctx);
        Vec3 cam = ctx.camera().getPosition();
        double radius = Math.max(2.0, m.num("Радиус"));
        String style = m.mode("Стиль");
        int col = m.color("Цвет");
        AABB area = mc.player.getBoundingBox().inflate(radius);
        for (ItemEntity it : mc.level.getEntitiesOfClass(ItemEntity.class, area)) {
            double x = Mth.lerp(pt, it.xOld, it.getX());
            double y = Mth.lerp(pt, it.yOld, it.getY());
            double z = Mth.lerp(pt, it.zOld, it.getZ());
            ps.pushPose();
            ps.translate(x - cam.x, y - cam.y, z - cam.z);
            if ("Свечение".equals(style)) {
                Render3D.discXZ(ps, buf, 0.005f, 0.28f, 24, Colors.withAlpha(col, 70));
            } else if ("Подпись".equals(style)) {
                int cnt = it.getItem().getCount();
                String label = cnt > 1 ? ("x" + cnt) : it.getItem().getHoverName().getString();
                billboardLocal(ps, buf, 0.35f, label, Colors.withAlpha(0xFFFFFFFF, 255));
            } else { // "Кольцо"
                float pulse = 0.5f + 0.5f * (float) Math.sin((System.currentTimeMillis() + it.getId() * 97L) / 300.0);
                Render3D.ringXZ(ps, buf, 0.01f, 0.18f, 0.24f + 0.05f * pulse, 24, Colors.withAlpha(col, 180));
            }
            ps.popPose();
        }
    }
}
