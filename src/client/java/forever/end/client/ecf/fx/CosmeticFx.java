package forever.end.client.ecf.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.ui.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Рендер косметики на игроках (себя и других). Все цвета — из настроек модуля (тема/радуга). */
public final class CosmeticFx {
    private CosmeticFx() {}

    private interface Each {
        void draw(PoseStack ps, MultiBufferSource buf, AbstractClientPlayer p, float partial);
    }

    /** Обход всех видимых игроков с локальным базисом (центр = ноги, поворот по корпусу). */
    private static void forEach(WorldRenderContext ctx, Each fn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 cam = ctx.camera().getPosition();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p.isInvisible()) continue;
            double px = Mth.lerp(partial, p.xOld, p.getX());
            double py = Mth.lerp(partial, p.yOld, p.getY());
            double pz = Mth.lerp(partial, p.zOld, p.getZ());
            float bodyYaw = Mth.lerp(partial, p.yBodyRotO, p.yBodyRot);
            ps.pushPose();
            ps.translate(px - cam.x, py - cam.y, pz - cam.z);
            ps.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
            fn.draw(ps, buf, p, partial);
            ps.popPose();
        }
    }

    private static float head(AbstractClientPlayer p) {
        return p.isCrouching() ? p.getBbHeight() - 0.28f : p.getBbHeight();
    }

    // ===== China Hat =====
    public static void chinaHat(WorldRenderContext ctx, Module m) {
        float size = m.numf("Размер");
        float off = m.numf("Высота");
        int col = m.color("Цвет");
        forEach(ctx, (ps, buf, p, pt) -> {
            float y = head(p) + off;
            Render3D.cone(ps, buf, y, 0.55f * size, 0.30f * size, 24, col);
        });
    }

    // ===== Halo =====
    public static void halo(WorldRenderContext ctx, Module m) {
        float size = m.numf("Размер");
        float off = m.numf("Высота");
        int col = m.color("Цвет");
        forEach(ctx, (ps, buf, p, pt) -> {
            float y = head(p) + 0.35f + off;
            Render3D.ringXZ(ps, buf, y, 0.26f * size, 0.34f * size, 40, col);
            Render3D.ringXZ(ps, buf, y + 0.001f, 0.30f * size, 0.32f * size, 40, Render3D.shade(col, 1.3f));
        });
    }

    // ===== Wings / Dragon Wings =====
    public static void wings(WorldRenderContext ctx, Module m) {
        float size = m.numf("Размер");
        float speed = m.numf("Скорость");
        float open = m.numf("Раскрытие");
        int col = m.color("Цвет");
        boolean dragon = m.bool("Дракон");
        forEach(ctx, (ps, buf, p, pt) -> {
            float flap = (float) Math.sin(System.currentTimeMillis() / 190.0 * Math.max(0.05, speed)) * 0.35f;
            drawWing(ps, buf, +1, col, size, flap, open, dragon);
            drawWing(ps, buf, -1, col, size, flap, open, dragon);
        });
    }

    private static void drawWing(PoseStack ps, MultiBufferSource buf, int side, int col, float size,
                                 float flap, float open, boolean dragon) {
        ps.pushPose();
        float rootY = 1.15f;
        float rootZ = -0.14f;
        ps.translate(side * 0.08f, rootY, rootZ);
        float spread = (18f + open * 42f + flap * 30f);
        ps.mulPose(Axis.ZP.rotationDegrees(side * spread));
        ps.mulPose(Axis.YP.rotationDegrees(side * 22f));
        int membrane = dragon ? Render3D.shade(col, 0.8f) : col;
        int edge = Render3D.shade(col, 1.25f);
        float s = size * (dragon ? 1.25f : 1.0f);
        // три секции крыла (веер)
        float[][] pts = {
            {0f, 0f, 0f},
            {side * 0.05f, 0.42f * s, -0.02f},
            {side * 0.55f * s, 0.55f * s, -0.05f},
            {side * 0.95f * s, 0.30f * s, -0.05f},
            {side * 1.05f * s, -0.12f * s, -0.05f},
            {side * 0.6f * s, -0.28f * s, -0.03f},
            {side * 0.18f * s, -0.30f * s, -0.01f}
        };
        for (int i = 1; i < pts.length - 1; i++) {
            Render3D.tri(ps, buf,
                    pts[0][0], pts[0][1], pts[0][2],
                    pts[i][0], pts[i][1], pts[i][2],
                    pts[i + 1][0], pts[i + 1][1], pts[i + 1][2],
                    (i % 2 == 0) ? membrane : Render3D.shade(membrane, 0.92f));
        }
        // верхняя кромка (тонкая полоса)
        Render3D.tri(ps, buf,
                pts[1][0], pts[1][1], pts[1][2],
                pts[2][0], pts[2][1], pts[2][2],
                pts[3][0], pts[3][1], pts[3][2], edge);
        ps.popPose();
    }

    // ===== Bunny Ears =====
    public static void bunnyEars(WorldRenderContext ctx, Module m) {
        float len = m.numf("Длина");
        int col = m.color("Цвет");
        int inner = Render3D.shade(col, 1.3f);
        forEach(ctx, (ps, buf, p, pt) -> {
            float y = head(p);
            for (int side = -1; side <= 1; side += 2) {
                ps.pushPose();
                ps.translate(side * 0.09f, y, 0f);
                ps.mulPose(Axis.ZP.rotationDegrees(side * 12f));
                Render3D.box(ps, buf, -0.045f, 0f, -0.03f, 0.045f, 0.34f * len, 0.03f, col);
                Render3D.quad(ps, buf,
                        -0.02f, 0.03f, 0.031f, 0.02f, 0.03f, 0.031f,
                        0.02f, 0.30f * len, 0.031f, -0.02f, 0.30f * len, 0.031f, inner);
                ps.popPose();
            }
        });
    }

    // ===== Backpack =====
    public static void backpack(WorldRenderContext ctx, Module m) {
        float size = m.numf("Размер");
        int col = m.color("Цвет");
        forEach(ctx, (ps, buf, p, pt) -> {
            Render3D.box(ps, buf,
                    -0.22f * size, 0.55f, -0.38f * size,
                    0.22f * size, 1.18f, -0.16f, col);
            // клапан
            Render3D.box(ps, buf,
                    -0.16f * size, 0.85f, -0.40f * size,
                    0.16f * size, 1.05f, -0.37f * size, Render3D.shade(col, 0.8f));
        });
    }

    // ===== Pet =====
    public static void pet(WorldRenderContext ctx, Module m) {
        float size = m.numf("Размер");
        float bobSpeed = m.numf("Скорость");
        int col = m.color("Цвет");
        String type = m.mode("Тип");
        forEach(ctx, (ps, buf, p, pt) -> {
            float bob = (float) Math.sin(System.currentTimeMillis() / 500.0 * Math.max(0.1, bobSpeed)) * 0.12f;
            float y = 1.1f + bob;
            ps.pushPose();
            ps.translate(0.55f, y, -0.42f);
            ps.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() % 3600L) / 10f));
            float h = 0.13f * size;
            if ("Звезда".equals(type)) {
                Render3D.discXZ(ps, buf, 0f, h * 1.4f, 5, col);
                Render3D.discXZ(ps, buf, 0.001f, h * 0.7f, 5, Render3D.shade(col, 1.3f));
            } else {
                Render3D.box(ps, buf, -h, -h, -h, h, h, h, col);
            }
            ps.popPose();
        });
    }
}
