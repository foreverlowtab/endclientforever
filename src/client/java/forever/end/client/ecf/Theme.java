package forever.end.client.ecf;

/** Цветовые темы клиента (порт токенов из HTML-прототипа). Все цвета в ARGB. */
public enum Theme {
    RED("Red",
        0xFFE11D2A, 0xFFFF414D,          // accent, accent2
        0xFFFFFFFF, 0xFFF6F7F9,          // panel, panel2
        0xFF14161C, 0xFF5B6270,          // text, muted
        0xFF2A0C10, 0xFFE11D2A, 0xFFFF414D), // pano3, pano1, pano2
    CLAUDE("Claude",
        0xFFCC785C, 0xFFD97757,
        0xFFFAF9F5, 0xFFF3F1E9,
        0xFF29261E, 0xFF6F6A5C,
        0xFF2B241D, 0xFFCC785C, 0xFFD97757);

    public final String label;
    public final int accent;
    public final int accent2;
    public final int panel;
    public final int panel2;
    public final int text;
    public final int muted;
    public final int pano3;
    public final int pano1;
    public final int pano2;

    // Аватар (зелёный градиент, одинаков для обеих тем — как на сайте).
    public final int avatarA = 0xFF46A171;
    public final int avatarB = 0xFF2F7350;

    Theme(String label, int accent, int accent2, int panel, int panel2, int text, int muted,
          int pano3, int pano1, int pano2) {
        this.label = label;
        this.accent = accent;
        this.accent2 = accent2;
        this.panel = panel;
        this.panel2 = panel2;
        this.text = text;
        this.muted = muted;
        this.pano3 = pano3;
        this.pano1 = pano1;
        this.pano2 = pano2;
    }

    /** Полупрозрачный акцент (accent-soft). */
    public int accentSoft() {
        return (accent & 0x00FFFFFF) | 0x24000000;
    }

    /** Цвет границы панелей (border). */
    public int border() {
        return (text & 0x00FFFFFF) | 0x1E000000;
    }

    /** Вторичная поверхность (surface-2) — чуть темнее panel2. */
    public int surface2() {
        return panel2;
    }
}
