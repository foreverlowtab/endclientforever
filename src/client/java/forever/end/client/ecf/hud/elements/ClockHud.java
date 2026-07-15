package forever.end.client.ecf.hud.elements;

import java.time.LocalDateTime;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Часы с секундами и днём недели — порт .hud-clock. По центру сверху. */
public class ClockHud extends HudElement {
    private static final String[] DOW = {"\u0412\u0421", "\u041f\u041d", "\u0412\u0422", "\u0421\u0420", "\u0427\u0422", "\u041f\u0422", "\u0421\u0411"};

    public ClockHud() { super("clock", "Clock", "Clock", Anchor.TC, 0, 6); }

    private static String pad(int x) { return (x < 10 ? "0" : "") + x; }

    private String[] build() {
        boolean sec = opt("\u0421\u0435\u043a\u0443\u043d\u0434\u044b", true);
        boolean date = opt("\u0414\u0430\u0442\u0430", false);
        String fmt = optMode("\u0424\u043e\u0440\u043c\u0430\u0442", "24\u0447");
        LocalDateTime n = LocalDateTime.now();
        int hh = n.getHour();
        if ("12\u0447".equals(fmt)) { hh = hh % 12; if (hh == 0) hh = 12; }
        String t = pad(hh) + ":" + pad(n.getMinute()) + (sec ? ":" + pad(n.getSecond()) : "");
        String d = date ? (DOW[n.getDayOfWeek().getValue() % 7] + " " + pad(n.getDayOfMonth()) + "." + pad(n.getMonthValue())) : "";
        return new String[]{t, d};
    }

    @Override
    protected void layout(Minecraft mc) {
        String[] s = build();
        float tw = HudManager.tw(mc.font, Fonts.grotesk(s[0]), 1.6f);
        float dw = s[1].isEmpty() ? 0 : HudManager.tw(mc.font, Fonts.body(s[1]), 0.8f);
        this.w = Math.round(Math.max(tw, dw)) + 18;
        this.h = s[1].isEmpty() ? 20 : 30;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        String[] s = build();
        HudManager.card(g, 0, 0, w, h);
        Font f = mc.font;
        HudManager.textCenter(g, f, Fonts.grotesk(s[0]), w / 2f, 5, 1.6f, HudManager.TXT);
        if (!s[1].isEmpty()) HudManager.textCenter(g, f, Fonts.body(s[1]), w / 2f, 20, 0.8f, HudManager.MUTED);
    }
}
