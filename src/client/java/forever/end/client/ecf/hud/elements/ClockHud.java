package forever.end.client.ecf.hud.elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Часы: реальное время (24/12ч, с секундами, опционально дата). */
public class ClockHud extends HudElement {
    private Component time, date;
    private int timeW, dateW;
    private boolean showDate;

    public ClockHud() { super("clock", "Clock", "Clock", 0.9f, 0.02f); }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        boolean seconds = opt("\u0421\u0435\u043a\u0443\u043d\u0434\u044b", true);
        showDate = opt("\u0414\u0430\u0442\u0430", false);
        boolean h12 = "12\u0447".equals(optMode("\u0424\u043e\u0440\u043c\u0430\u0442", "24\u0447"));
        String pat = (h12 ? "hh:mm" : "HH:mm") + (seconds ? ":ss" : "") + (h12 ? " a" : "");
        LocalDateTime now = LocalDateTime.now();
        time = Fonts.display(now.format(DateTimeFormatter.ofPattern(pat)));
        date = Fonts.body(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        timeW = f.width(time);
        dateW = f.width(date);
        w = 8 + 10 + 5 + Math.max(timeW, showDate ? dateW : 0) + 8;
        h = 6 + 10 + (showDate ? 3 + 9 : 0) + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        int acc = Colors.themeAccent();
        HudManager.glassAccent(g, 0, 0, w, h, acc);
        int x = 8, y = 6;
        Component clk = Component.literal("\u23f1");
        g.drawString(f, clk, x, y + 1, acc, false);
        int tx = x + 10 + 5;
        g.drawString(f, time, tx, y, 0xFFFFFFFF, true);
        if (showDate) {
            g.drawString(f, date, tx, y + 12, HudManager.MUTED, false);
        }
    }
}
