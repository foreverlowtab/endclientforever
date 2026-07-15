package forever.end.client.ecf.hud.elements;

import java.util.Locale;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Вотермарк: логотип + версия, профиль с аватаром, подпиской и ролью — порт .hud-wm. Верх слева. */
public class WatermarkHud extends HudElement {
    public WatermarkHud() { super("watermark", "Watermark", "Watermark", Anchor.TL, 6, 6); }

    private static String verStr() { return "0.6-test \u00b7 fabric 1.21.4"; }

    private static String name() {
        String n = ClientState.displayName;
        if (n == null || n.isBlank()) n = ClientState.username;
        if (n == null || n.isBlank()) n = "\u0413\u043e\u0441\u0442\u044c";
        return n;
    }

    private static String plan() {
        String p = ClientState.subActive ? ClientState.subPlan : "FREE";
        return p == null ? "FREE" : p.toUpperCase(Locale.ROOT);
    }

    private static String role() {
        String r = ClientState.roleLabel;
        return (r == null || r.isBlank()) ? "" : r.toUpperCase(Locale.ROOT);
    }

    private static String initials() {
        String n = name();
        String[] parts = n.trim().split("\\s+");
        String a = parts[0].substring(0, 1);
        String b = parts.length > 1 ? parts[1].substring(0, 1) : (parts[0].length() > 1 ? parts[0].substring(1, 2) : "");
        return (a + b).toUpperCase(Locale.ROOT);
    }

    private float badgeW(Font f, String text) { return HudManager.tw(f, Fonts.body(text), 0.6f) + 8; }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        boolean ver = opt("\u0412\u0435\u0440\u0441\u0438\u044f", true);
        boolean prof = opt("\u041f\u0440\u043e\u0444\u0438\u043b\u044c", true);
        float wmax = 23 + HudManager.tw(f, Fonts.grotesk("End Client Forever"), 1.15f);
        if (ver) wmax = Math.max(wmax, 23 + HudManager.tw(f, Fonts.body(verStr()), 0.75f));
        if (prof) {
            wmax = Math.max(wmax, 25 + HudManager.tw(f, Fonts.grotesk(name()), 0.9f) + 9);
            wmax = Math.max(wmax, 25 + badgeW(f, plan()) + 4 + badgeW(f, role()));
        }
        this.w = Math.round(wmax) + 7;
        this.h = prof ? 58 : (ver ? 26 : 20);
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        boolean ver = opt("\u0412\u0435\u0440\u0441\u0438\u044f", true);
        boolean prof = opt("\u041f\u0440\u043e\u0444\u0438\u043b\u044c", true);
        HudManager.card(g, 0, 0, w, h);
        int acc = HudManager.accent();
        Draw.roundRect(g, 7, 5, 12, 12, 3, acc);
        g.drawString(f, Component.literal("\u2726"), 10, 7, 0xFFFFFFFF, false);
        HudManager.text(g, f, Fonts.grotesk("End Client Forever"), 23, 4, 1.15f, 0xFFFFFFFF);
        if (ver) HudManager.text(g, f, Fonts.body(verStr()), 23, 15, 0.75f, HudManager.VER);
        if (prof) {
            int dy = 26;
            g.fill(7, dy, w - 7, dy + 1, HudManager.BORDER);
            int py = 31;
            Draw.roundRect(g, 7, py, 14, 14, 4, acc);
            HudManager.textCenter(g, f, Fonts.grotesk(initials()), 7 + 7, py + 4, 0.7f, 0xFFFFFFFF);
            float nameW = HudManager.tw(f, Fonts.grotesk(name()), 0.9f);
            HudManager.text(g, f, Fonts.grotesk(name()), 25, py - 1, 0.9f, 0xFFFFFFFF);
            Draw.roundRect(g, Math.round(25 + nameW + 3), py + 2, 3, 3, 1, HudManager.ONLINE);
            int by = py + 12;
            int bx = 25;
            bx = badge(g, f, bx, by, plan(), true);
            String r = role();
            if (!r.isEmpty()) badge(g, f, bx + 4, by, r, false);
        }
    }

    private int badge(GuiGraphics g, Font f, int x, int y, String text, boolean planStyle) {
        int bw = Math.round(HudManager.tw(f, Fonts.body(text), 0.6f)) + 8;
        int bh = 9;
        if (planStyle) {
            Draw.pill(g, x, y, bw, bh, HudManager.accent());
            HudManager.text(g, f, Fonts.body(text), x + 4, y + 2, 0.6f, 0xFFFFFFFF);
        } else {
            Draw.pillBorder(g, x, y, bw, bh, 0x14FFFFFF, HudManager.accent());
            HudManager.text(g, f, Fonts.body(text), x + 4, y + 2, 0.6f, HudManager.accent());
        }
        return x + bw;
    }
}
