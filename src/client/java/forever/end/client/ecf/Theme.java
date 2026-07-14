package forever.end.client.ecf;

/** Цветовые темы клиента (порт из HTML-прототипа). ARGB цвета. */
public enum Theme {
    RED("Red", 0xFFE11D2A, 0xFFFF414D, 0xFFFFFFFF, 0xFFF6F7F9, 0xFF14161C, 0xFF5B6270),
    CLAUDE("Claude", 0xFFCC785C, 0xFFD97757, 0xFFFAF9F5, 0xFFF3F1E9, 0xFF29261E, 0xFF6F6A5C);

    public final String label;
    public final int accent;
    public final int accent2;
    public final int panel;
    public final int panel2;
    public final int text;
    public final int muted;

    Theme(String label, int accent, int accent2, int panel, int panel2, int text, int muted) {
        this.label = label;
        this.accent = accent;
        this.accent2 = accent2;
        this.panel = panel;
        this.panel2 = panel2;
        this.text = text;
        this.muted = muted;
    }

    /** Полупрозрачный акцент (для подсветки включённых модулей). */
    public int accentSoft() {
        return (accent & 0x00FFFFFF) | 0x33000000;
    }

    /** Цвет границы панелей. */
    public int border() {
        return (text & 0x00FFFFFF) | 0x22000000;
    }
}
