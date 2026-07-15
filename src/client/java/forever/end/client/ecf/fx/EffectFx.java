package forever.end.client.ecf.fx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Логика и рендер эффектов. Цвета берутся из настроек модуля (тема/радуга/ручной). */
public final class EffectFx {
    private EffectFx() {}

    private interface PointDraw { void draw(PoseStack ps, MultiBufferSource buf); }

    // ---- состояние ----
    private static boolean prevJumpGround = true;
    private static final List<Ring> rings = new ArrayList<>();
    private static final List<Node> trail = new ArrayList<>();
    private static final List<Foot> foots = new ArrayList<>();
    private static int footTimer = 0;
    private static boolean leftFoot = false;
    private static float prevHealth = -1f;
    private static long dmgFlashUntil = 0L;
    private static long hitFlashUntil = 0L;
    private static Entity lastTarget = null;
    private static int killWatch = 0;

    private static final class Ring { double x, y, z; int age; int maxAge; float maxR; }
    private static final class Node { double x, y, z; }
    private static final class Foot { double x, y, z; float yaw; int age; int maxAge; }

    private static void worldPoint(WorldRenderContext ctx, double wx, double wy, double wz, PointDraw fn) {
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        Vec3 cam = ctx.camera().getPosition();
        ps.pushPose();
        ps.translate(wx - cam.x, wy - cam.y, wz - cam.z);
        fn.draw(ps, buf);
        ps.popPose();
    }

    private static float partial(WorldRenderContext ctx) {
        return ctx.tickCounter().getGameTimeDeltaPartialTick(false);
    }

    // ===================== Jump Circles =====================
    public static void jumpTick(Minecraft mc, Module m) {
        var pl = mc.player;
        if (pl != null) {
            boolean ground = pl.onGround();
            if (prevJumpGround && !ground && pl.getDeltaMovement().y > 0.08) {
                Ring r = new Ring();
                r.x = pl.getX(); r.y = pl.getY() + 0.05; r.z = pl.getZ();
                r.maxAge = Math.max(4, m.inti("Длительность"));
                r.maxR = m.numf("Размер");
                rings.add(r);
            }
            prevJumpGround = ground;
        }
        for (Iterator<Ring> it = rings.iterator(); it.hasNext(); ) {
            Ring r = it.next();
            if (++r.age >= r.maxAge) it.remove();
        }
        if (rings.size() > 40) rings.subList(0, rings.size() - 40).clear();
    }

    public static void jumpRender(WorldRenderContext ctx, Module m) {
        float pt = partial(ctx);
        int base = m.color("Цвет");
        for (Ring r : rings) {
            float prog = Math.min(1f, (r.age + pt) / Math.max(1, r.maxAge));
            float rad = 0.2f + prog * r.maxR;
            int a = (int) (200 * (1f - prog));
            int c = Colors.withAlpha(base, Math.max(0, a));
            worldPoint(ctx, r.x, r.y, r.z, (ps, buf) -> Render3D.ringXZ(ps, buf, 0.02f, rad * 0.85f, rad, 44, c));
        }
    }

    // ===================== Motion Trail =====================
    public static void trailTick(Minecraft mc, Module m) {
        var pl = mc.player;
        if (pl != null) {
            double sp = Math.hypot(pl.getX() - pl.xOld, pl.getZ() - pl.zOld);
            if (sp > 0.03) {
                Node n = new Node();
                n.x = pl.getX(); n.y = pl.getY() + 1.0; n.z = pl.getZ();
                trail.add(n);
            }
        }
        int max = Math.max(3, m.inti("Длина"));
        while (trail.size() > max) trail.remove(0);
    }

    public static void trailRender(WorldRenderContext ctx, Module m) {
        int base = m.color("Цвет");
        float w = m.numf("Ширина");
        int n = trail.size();
        for (int i = 0; i < n; i++) {
            Node nd = trail.get(i);
            float f = (i + 1f) / n;
            int c = Colors.withAlpha(base, (int) (170 * f));
            float rad = 0.05f + w * 0.16f * f;
            worldPoint(ctx, nd.x, nd.y, nd.z, (ps, buf) -> Render3D.discXZ(ps, buf, 0f, rad, 12, c));
        }
    }

    // ===================== Footprints =====================
    public static void footTick(Minecraft mc, Module m) {
        var pl = mc.player;
        if (pl != null) {
            boolean ground = pl.onGround();
            double sp = Math.hypot(pl.getX() - pl.xOld, pl.getZ() - pl.zOld);
            if (ground && sp > 0.02) {
                if (++footTimer >= 5) {
                    footTimer = 0;
                    leftFoot = !leftFoot;
                    float yaw = pl.getYRot();
                    double rad = Math.toRadians(yaw);
                    double rx = Math.cos(rad) * 0.16 * (leftFoot ? 1 : -1);
                    double rz = Math.sin(rad) * 0.16 * (leftFoot ? 1 : -1);
                    Foot f = new Foot();
                    f.x = pl.getX() + rx; f.y = pl.getY() + 0.02; f.z = pl.getZ() + rz;
                    f.yaw = yaw;
                    f.maxAge = Math.max(10, m.inti("Длительность"));
                    foots.add(f);
                }
            }
        }
        for (Iterator<Foot> it = foots.iterator(); it.hasNext(); ) {
            Foot f = it.next();
            if (++f.age >= f.maxAge) it.remove();
        }
        if (foots.size() > 60) foots.subList(0, foots.size() - 60).clear();
    }

    public static void footRender(WorldRenderContext ctx, Module m) {
        float pt = partial(ctx);
        int base = m.color("Цвет");
        float size = m.numf("Размер");
        for (Foot f : foots) {
            float prog = Math.min(1f, (f.age + pt) / Math.max(1, f.maxAge));
            int c = Colors.withAlpha(base, (int) (150 * (1f - prog)));
            worldPoint(ctx, f.x, f.y, f.z, (ps, buf) -> {
                ps.mulPose(Axis.YP.rotationDegrees(-f.yaw));
                float hw = 0.06f * size, hl = 0.10f * size;
                Render3D.quad(ps, buf, -hw, 0.001f, -hl, -hw, 0.001f, hl, hw, 0.001f, hl, hw, 0.001f, -hl, c);
            });
        }
    }

    // ===================== Damage Particles =====================
    public static void dmgTick(Minecraft mc, Module m) {
        var pl = mc.player;
        if (pl == null) return;
        float hp = pl.getHealth();
        if (prevHealth < 0) prevHealth = hp;
        if (hp < prevHealth - 0.01f) {
            int col = m.color("Цвет");
            int count = Math.max(4, m.inti("Количество"));
            dust(mc.level, pl.getX(), pl.getY() + 1.0, pl.getZ(), col, count, 0.6, 0.14);
            if (m.bool("Вспышка экрана")) dmgFlashUntil = System.currentTimeMillis() + 400L;
        }
        prevHealth = hp;
    }

    public static void dmgHud(GuiGraphics g, float partial, Module m) {
        long now = System.currentTimeMillis();
        if (now >= dmgFlashUntil) return;
        float f = (dmgFlashUntil - now) / 400f;
        vignette(g, Colors.withAlpha(m.color("Цвет"), (int) (130 * f)));
    }

    // ===================== Hit Effect =====================
    public static void hitHud(GuiGraphics g, float partial, Module m) {
        long now = System.currentTimeMillis();
        if (now >= hitFlashUntil) return;
        float f = (hitFlashUntil - now) / 250f;
        vignette(g, Colors.withAlpha(m.color("Цвет"), (int) (90 * f)));
    }

    // ===================== Kill Effect =====================
    public static void killTick(Minecraft mc, Module m) {
        if (killWatch > 0) {
            killWatch--;
            if (lastTarget != null && (!lastTarget.isAlive() || lastTarget.isRemoved())) {
                spawnKill(mc, m, lastTarget.getX(), lastTarget.getY() + lastTarget.getBbHeight() * 0.5, lastTarget.getZ());
                lastTarget = null;
                killWatch = 0;
            }
        }
    }

    private static void spawnKill(Minecraft mc, Module m, double x, double y, double z) {
        if (mc.level == null) return;
        String mode = m.mode("Режим");
        int col = m.color("Цвет");
        if ("Фейерверк".equals(mode)) {
            for (int i = 0; i < 3; i++) mc.level.addParticle(ParticleTypes.FIREWORK, x, y, z, (rnd() - 0.5) * 0.3, rnd() * 0.3, (rnd() - 0.5) * 0.3);
            dust(mc.level, x, y, z, col, 24, 0.4, 0.35);
        } else if ("Молния".equals(mode)) {
            for (int i = 0; i < 14; i++) dust(mc.level, x, y + i * 0.14, z, 0xFFFFFFFF, 1, 0.06, 0.02);
            dust(mc.level, x, y, z, col, 16, 0.3, 0.25);
        } else { // Взрыв
            dust(mc.level, x, y, z, col, 30, 0.5, 0.4);
            for (int i = 0; i < 6; i++) mc.level.addParticle(ParticleTypes.CRIT, x, y, z, (rnd() - 0.5) * 0.4, rnd() * 0.4, (rnd() - 0.5) * 0.4);
        }
    }

    /** Вызывается из ModuleManager при атаке игрока по сущности. */
    public static void onAttack(Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || target == null) return;
        lastTarget = target;
        killWatch = 15;
        Module hit = Modules.find("Hit Effect");
        if (hit != null && hit.enabled) {
            String mode = hit.mode("Режим");
            int col = hit.color("Цвет");
            int count = Math.max(3, hit.inti("Количество"));
            double x = target.getX(), y = target.getY() + target.getBbHeight() * 0.6, z = target.getZ();
            spawnHit(mc, mode, col, count, x, y, z);
            if (hit.bool("Вспышка экрана")) hitFlashUntil = System.currentTimeMillis() + 250L;
        }
    }

    private static void spawnHit(Minecraft mc, String mode, int col, int count, double x, double y, double z) {
        if (mc.level == null) return;
        ParticleOptions vanilla = null;
        if ("Крит".equals(mode)) vanilla = ParticleTypes.ENCHANTED_HIT;
        else if ("Сердца".equals(mode)) vanilla = ParticleTypes.HEART;
        else vanilla = ParticleTypes.CRIT;
        for (int i = 0; i < count; i++) {
            mc.level.addParticle(vanilla, x, y, z, (rnd() - 0.5) * 0.4, rnd() * 0.4, (rnd() - 0.5) * 0.4);
        }
        dust(mc.level, x, y, z, col, count, 0.35, 0.3);
    }

    // ===================== Block Overlay =====================
    public static void blockRender(WorldRenderContext ctx, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult bhr = (BlockHitResult) mc.hitResult;
        BlockPos pos = bhr.getBlockPos();
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buf = ctx.consumers();
        if (ps == null || buf == null) return;
        int base = m.color("Цвет");
        int op = Math.max(6, m.inti("Прозрачность"));
        boolean outline = m.bool("Только контур");
        Vec3 cam = ctx.camera().getPosition();
        ps.pushPose();
        ps.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
        float e = 0.003f;
        if (outline) {
            frame(ps, buf, -e, -e, -e, 1 + e, 1 + e, 1 + e, 0.03f, Colors.withAlpha(base, 235));
        } else {
            Render3D.box(ps, buf, -e, -e, -e, 1 + e, 1 + e, 1 + e, Colors.withAlpha(base, Math.min(180, op * 2)));
        }
        ps.popPose();
    }

    /** Каркас из 12 тонких рёбер. */
    private static void frame(PoseStack ps, MultiBufferSource buf, float x0, float y0, float z0,
                              float x1, float y1, float z1, float t, int c) {
        // 4 вертикальных
        Render3D.box(ps, buf, x0, y0, z0, x0 + t, y1, z0 + t, c);
        Render3D.box(ps, buf, x1 - t, y0, z0, x1, y1, z0 + t, c);
        Render3D.box(ps, buf, x0, y0, z1 - t, x0 + t, y1, z1, c);
        Render3D.box(ps, buf, x1 - t, y0, z1 - t, x1, y1, z1, c);
        // 4 нижних
        Render3D.box(ps, buf, x0, y0, z0, x1, y0 + t, z0 + t, c);
        Render3D.box(ps, buf, x0, y0, z1 - t, x1, y0 + t, z1, c);
        Render3D.box(ps, buf, x0, y0, z0, x0 + t, y0 + t, z1, c);
        Render3D.box(ps, buf, x1 - t, y0, z0, x1, y0 + t, z1, c);
        // 4 верхних
        Render3D.box(ps, buf, x0, y1 - t, z0, x1, y1, z0 + t, c);
        Render3D.box(ps, buf, x0, y1 - t, z1 - t, x1, y1, z1, c);
        Render3D.box(ps, buf, x0, y1 - t, z0, x0 + t, y1, z1, c);
        Render3D.box(ps, buf, x1 - t, y1 - t, z0, x1, y1, z1, c);
    }

    // ===================== хелперы =====================
    private static double rnd() { return Math.random(); }

    private static void dust(Level level, double x, double y, double z, int color, int count, double spread, double speed) {
        if (level == null) return;
        DustParticleOptions opt = new DustParticleOptions(color & 0xFFFFFF, 1.0f);
        for (int i = 0; i < count; i++) {
            level.addParticle(opt,
                    x + (rnd() - 0.5) * spread,
                    y + (rnd() - 0.5) * spread,
                    z + (rnd() - 0.5) * spread,
                    (rnd() - 0.5) * speed, rnd() * speed, (rnd() - 0.5) * speed);
        }
    }

    private static void vignette(GuiGraphics g, int color) {
        int w = g.guiWidth(), h = g.guiHeight();
        int trans = color & 0x00FFFFFF;
        int thick = Math.max(24, h / 6);
        g.fillGradient(0, 0, w, thick, color, trans);
        g.fillGradient(0, h - thick, w, h, trans, color);
        int sideA = Colors.withAlpha(color, ((color >>> 24) & 0xFF) / 2);
        g.fill(0, 0, thick / 2, h, sideA);
        g.fill(w - thick / 2, 0, w, h, sideA);
    }
}
