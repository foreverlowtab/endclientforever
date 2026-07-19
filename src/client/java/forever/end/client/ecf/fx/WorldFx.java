package forever.end.client.ecf.fx;

import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * \u0420\u0435\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u044f \u043a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u0438 World (\u0431\u0435\u0437 \u043c\u0438\u043a\u0441\u0438\u043d\u043e\u0432 \u2014 \u0442\u043e\u043b\u044c\u043a\u043e \u0431\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u044b\u0435 \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u044b\u0435 API).
 *  - Fullbright      -> gamma boost (options.gamma).
 *  - Time Changer    -> \u043a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u0441\u0443\u0442\u043e\u043a (level.setDayTime).
 *  - Weather Changer -> \u043a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u0430\u044f \u043f\u043e\u0433\u043e\u0434\u0430 (level.setRainLevel/ThunderLevel).
 *  - Custom Sky      -> \u044d\u043a\u0440\u0430\u043d\u043d\u044b\u0439 \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u043d\u044b\u0439 \u043e\u0432\u0435\u0440\u043b\u0435\u0439 (HUD).
 *  - Custom Clouds   -> \u0440\u0435\u0436\u0438\u043c \u043e\u0431\u043b\u0430\u043a\u043e\u0432 (options.cloudStatus).
 *  - Bloom           -> \u043c\u044f\u0433\u043a\u043e\u0435 \u044d\u043a\u0440\u0430\u043d\u043d\u043e\u0435 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435 (HUD).
 */
public final class WorldFx {
    private WorldFx() {}

    // \u0421\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u044b\u0435 \u0438\u0441\u0445\u043e\u0434\u043d\u044b\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f \u2014 \u0447\u0442\u043e\u0431\u044b \u0447\u0435\u0441\u0442\u043d\u043e \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u043f\u0440\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438.
    private static Double savedGamma = null;
    private static CloudStatus savedClouds = null;

    // ---------------- Fullbright ----------------
    public static void fullbrightTick(Minecraft mc, Module m) {
        if (mc.options == null) return;
        if (savedGamma == null) savedGamma = mc.options.gamma().get();
        double target = m.num("\u042f\u0440\u043a\u043e\u0441\u0442\u044c");
        if (target <= 0) target = 1.0;
        mc.options.gamma().set(target);
    }

    public static void fullbrightReset(Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null && savedGamma != null) mc.options.gamma().set(savedGamma);
        savedGamma = null;
    }

    // ---------------- Time Changer ----------------
    public static void timeTick(Minecraft mc, Module m) {
        if (mc.level == null) return;
        long t;
        switch (m.mode("\u0412\u0440\u0435\u043c\u044f")) {
            case "\u041f\u043e\u043b\u0434\u0435\u043d\u044c" -> t = 6000L;
            case "\u0417\u0430\u043a\u0430\u0442"   -> t = 12000L;
            case "\u041d\u043e\u0447\u044c"    -> t = 14000L;
            case "\u041f\u043e\u043b\u043d\u043e\u0447\u044c" -> t = 18000L;
            default -> t = 23000L; // \u0423\u0442\u0440\u043e / \u0440\u0430\u0441\u0441\u0432\u0435\u0442
        }
        long day = mc.level.getDayTime() / 24000L;
        mc.level.setDayTime(day * 24000L + t);
    }

    // ---------------- Weather Changer ----------------
    public static void weatherTick(Minecraft mc, Module m) {
        if (mc.level == null) return;
        switch (m.mode("\u041f\u043e\u0433\u043e\u0434\u0430")) {
            case "\u0414\u043e\u0436\u0434\u044c" -> { mc.level.setRainLevel(1f); mc.level.setThunderLevel(0f); }
            case "\u0413\u0440\u043e\u0437\u0430" -> { mc.level.setRainLevel(1f); mc.level.setThunderLevel(1f); }
            default -> { mc.level.setRainLevel(0f); mc.level.setThunderLevel(0f); }
        }
    }

    public static void weatherReset(Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) { mc.level.setRainLevel(0f); mc.level.setThunderLevel(0f); }
    }

    // ---------------- Custom Clouds ----------------
    public static void cloudsTick(Minecraft mc, Module m) {
        if (mc.options == null) return;
        if (savedClouds == null) savedClouds = mc.options.cloudStatus().get();
        CloudStatus want;
        switch (m.mode("\u041e\u0431\u043b\u0430\u043a\u0430")) {
            case "\u0412\u044b\u043a\u043b" -> want = CloudStatus.OFF;
            case "\u0411\u044b\u0441\u0442\u0440\u044b\u0435" -> want = CloudStatus.FAST;
            default -> want = CloudStatus.FANCY;
        }
        if (mc.options.cloudStatus().get() != want) mc.options.cloudStatus().set(want);
    }

    public static void cloudsReset(Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null && savedClouds != null) mc.options.cloudStatus().set(savedClouds);
        savedClouds = null;
    }

    // ---------------- Custom Sky (HUD) ----------------
    public static void skyHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float density = m.numf("\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c");
        if (density <= 0f) return;
        int col = m.color("\u0426\u0432\u0435\u0442");
        int w = g.guiWidth();
        int h = g.guiHeight();
        int topA = (int) Math.min(255f, density * 255f);
        int top = Draw.alpha(col, topA);
        int bottom = Draw.alpha(col, 0);
        g.fillGradient(0, 0, w, h, top, bottom);
    }

    // ---------------- Bloom (HUD) ----------------
    public static void bloomHud(GuiGraphics g, float partial, Module m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float intensity = m.numf("\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c");
        if (intensity <= 0f) return;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int haze = Draw.alpha(0xFFFFFFFF, (int) (intensity * 34f));
        Draw.rect(g, 0, 0, w, h, haze);
        int glowTop = Draw.alpha(0xFFFFFFFF, (int) (intensity * 60f));
        g.fillGradient(0, 0, w, Math.max(24, h / 3), glowTop, Draw.alpha(0xFFFFFFFF, 0));
    }
}
