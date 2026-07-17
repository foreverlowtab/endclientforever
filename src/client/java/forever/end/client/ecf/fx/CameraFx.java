package forever.end.client.ecf.fx;

import org.lwjgl.glfw.GLFW;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Реализация категории Camera. Часть модулей опирается на миксины
 * (чтобы было удобнее и красивее):
 *
 *  - Zoom / FOV Changer -> {@code GameRendererMixin} вызывает {@link #applyFov(float)}
 *                          каждый кадр (плавный зум без ванильного лимита 30–110).
 *  - Freelook           -> {@code CameraMixin} читает {@link #freeYaw()}/{@link #freePitch()},
 *                          {@code EntityTurnMixin} перенаправляет мышь в {@link #addFreeLook}.
 *  - No Hurt Cam        -> options.damageTiltStrength.
 *  - Cinematic Camera   -> options.smoothCamera.
 *  - Motion Blur        -> экранный HUD-оверлей (без миксина).
 */
public final class CameraFx {
    private CameraFx() {}

    // No Hurt Cam / Cinematic — сохранённые исходные значения.
    private static Double savedTilt = null;
    private static Boolean savedSmooth = null;

    // Zoom — плавный множитель обзора (1 = без зума).
    private static float zoomAnim = 1f;

    // Freelook.
    private static boolean freelookActive = false;
    private static float freeYaw = 0f;
    private static float freePitch = 0f;

    // Motion Blur.
    private static float prevYaw = 0f;
    private static float prevPitch = 0f;
    private static float blurAmt = 0f;

    private static boolean on(Module m) {
        return m != null && m.enabled;
    }

    private static boolean keyHeld(Minecraft mc, int key) {
        if (mc.screen != null || mc.getWindow() == null) return false;
        return GLFW.glfwGetKey(mc.getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
    }

    // ---------- Freelook API (для миксинов) ----------
    public static boolean freelookActive() {
        return freelookActive;
    }

    public static float freeYaw() {
        return freeYaw;
    }

    public static float freePitch() {
        return freePitch;
    }

    /** Накапливает движение мыши в свободный обзор (та же чувствительность, что и у ванили). */
    public static void addFreeLook(double dYaw, double dPitch) {
        freeYaw += (float) (dYaw * 0.15);
        freePitch += (float) (dPitch * 0.15);
        freePitch = Mth.clamp(freePitch, -90f, 90f);
    }

    // ---------- FOV (для GameRendererMixin), каждый кадр ----------
    public static float applyFov(float base) {
        Minecraft mc = Minecraft.getInstance();
        float out = base;

        // FOV Changer — масштабируем обзор (сохраняя эффекты спринта), без лимита 110.
        Module fovCh = Modules.find("FOV Changer");
        if (on(fovCh) && mc.options != null) {
            int optFov = mc.options.fov().get();
            if (optFov > 0) out *= (float) fovCh.num("FOV") / optFov;
        }

        // Zoom — плавный множитель по удержанию клавиши, обходит ванильный кламп.
        Module zoom = Modules.find("Zoom");
        boolean held = on(zoom) && mc.level != null && keyHeld(mc, GLFW.GLFW_KEY_C);
        float factor = zoom != null ? (float) Math.max(1.01, zoom.num("Кратность")) : 2f;
        float target = held ? 1f / factor : 1f;
        boolean smooth = zoom == null || zoom.bool("Плавно");
        if (smooth) {
            float sp = (float) (zoom != null ? zoom.num("Скорость") : 0.5);
            sp = Mth.clamp(sp, 0.05f, 1f);
            zoomAnim += (target - zoomAnim) * sp;
        } else {
            zoomAnim = target;
        }
        out *= zoomAnim;
        return out;
    }

    // ---------- Тик: No Hurt Cam, Cinematic, старт/стоп Freelook ----------
    public static void onClientTick(Minecraft mc) {
        if (mc == null || mc.options == null) return;
        Options o = mc.options;

        Module noHurt = Modules.find("No Hurt Cam");
        if (on(noHurt)) {
            if (savedTilt == null) savedTilt = o.damageTiltStrength().get();
            o.damageTiltStrength().set(0.0);
        } else if (savedTilt != null) {
            o.damageTiltStrength().set(savedTilt);
            savedTilt = null;
        }

        Module cine = Modules.find("Cinematic Camera");
        if (on(cine)) {
            if (savedSmooth == null) savedSmooth = o.smoothCamera;
            o.smoothCamera = true;
        } else if (savedSmooth != null) {
            o.smoothCamera = savedSmooth;
            savedSmooth = null;
        }

        Module free = Modules.find("Freelook");
        boolean freeHeld = on(free) && mc.level != null && mc.player != null
                && keyHeld(mc, GLFW.GLFW_KEY_V);
        if (freeHeld && !freelookActive) {
            freeYaw = mc.player.getYRot();
            freePitch = mc.player.getXRot();
            freelookActive = true;
        } else if (!freeHeld && freelookActive) {
            freelookActive = false;
        }
    }

    // ---------- Motion Blur (HUD) ----------
    public static void motionHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null) return;

        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        float dyaw = Math.abs(Mth.wrapDegrees(yaw - prevYaw));
        float dp = Math.abs(pitch - prevPitch);
        prevYaw = yaw;
        prevPitch = pitch;

        float target = Math.min(1f, (dyaw + dp) / 14f);
        blurAmt += (target - blurAmt) * 0.35f;
        if (blurAmt < 0.02f) return;

        int intensity = Math.max(10, m.inti("Интенсивность"));
        int a = (int) (intensity / 100f * 170f * blurAmt);
        if (a <= 1) return;
        int col = (Math.min(255, a) << 24);
        int w = g.guiWidth();
        int h = g.guiHeight();
        int band = Math.max(24, w / 5);
        g.fillGradient(0, 0, band, h, col, 0);
        g.fillGradient(w - band, 0, w, h, 0, col);
        int vband = Math.max(16, h / 8);
        g.fillGradient(0, 0, w, vband, col, 0);
        g.fillGradient(0, h - vband, w, h, 0, col);
    }
}
