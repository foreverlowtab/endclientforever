package forever.end.client.ecf.ui;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.module.setting.Setting;

/** Разрешение цветов модулей: тема / радуга / ручной выбор. */
public final class Colors {
    private Colors() {}

    /** Радужный ARGB со сдвигом фазы (0..1). */
    public static int rainbow(float offset, float sat, float bri) {
        float hue = ((System.currentTimeMillis() % 4000L) / 4000f + offset) % 1f;
        if (hue < 0) hue += 1f;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, sat, bri) & 0xFFFFFF);
    }

    public static int rainbow(float offset) {
        return rainbow(offset, 0.85f, 1f);
    }

    /** Включён ли модуль Chroma (глобальная радуга акцентов). */
    public static boolean chromaOn() {
        Module c = Modules.find("Chroma");
        return c != null && c.enabled;
    }

    /** Радужный акцент Chroma со сдвигом фазы (0..1). Скорость/насыщенность — из настроек модуля. */
    public static int chroma(float offset) {
        Module c = Modules.find("Chroma");
        float speed = (c != null && c.numf("Скорость") > 0f) ? c.numf("Скорость") : 1f;
        float sat = (c != null && c.numf("Насыщенность") > 0f) ? c.numf("Насыщенность") : 0.85f;
        long period = (long) (4000f / Math.max(0.05f, speed));
        if (period < 200L) period = 200L;
        float hue = ((System.currentTimeMillis() % period) / (float) period + offset) % 1f;
        if (hue < 0f) hue += 1f;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, Math.min(1f, sat), 1f) & 0xFFFFFF);
    }

    /** Акцент активной темы (ARGB, полная альфа). С учётом Chroma. */
    public static int themeAccent() {
        if (chromaOn()) return chroma(0f);
        return 0xFF000000 | (ClientState.theme.accent & 0xFFFFFF);
    }

    /** Вторичный акцент активной темы (ARGB, полная альфа). С учётом Chroma. */
    public static int themeAccent2() {
        if (chromaOn()) return chroma(0.08f);
        return 0xFF000000 | (ClientState.theme.accent2 & 0xFFFFFF);
    }

    /** Итоговый цвет для настройки Color с учётом темы/радуги/ручного. */
    public static int resolve(Setting.Color c) {
        if (c == null) return themeAccent();
        if (c.rainbow) return rainbow(0f);
        if (c.syncTheme) return themeAccent();
        return 0xFF000000 | (c.argb & 0xFFFFFF);
    }

    public static int withAlpha(int argb, int a) {
        return (argb & 0x00FFFFFF) | ((a & 0xFF) << 24);
    }

    /** ARGB из HSB (полная альфа). */
    public static int fromHue(float hue) {
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.85f, 1f) & 0xFFFFFF);
    }

    /** Грубая оценка hue у ARGB-цвета (для позиционирования ползунка). */
    public static float hueOf(int argb) {
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
        return hsb[0];
    }
}
