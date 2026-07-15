package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Водяной знак: бренд + версия + строка профиля (имя / подписка / роль). */
public class WatermarkHud extends HudElement {
    private Component title, ver, name, plan, role;
    private int titleW, verW, nameW, planW, roleW;
    private boolean profile, showVer;

    public WatermarkHud() { super("watermark", "Watermark", "Watermark", 0.008f, 0.012f); }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        profile = opt("\u041f\u0440\u043e\u0444\u0438\u043b\u044c", true);
        showVer = opt("\u0412\u0435\u0440\u0441\u0438\u044f", true);
        title = Fonts.display("End Client Forever");
        ver = Fonts.body("0.6-test");
        String nm = !ClientState.displayName.isEmpty() ? ClientState.displayName
                : (ClientState.username.isEmpty() ? "Player" : ClientState.username);
        name = Fonts.display(nm);
        plan = Fonts.body("\u25c6 " + (ClientState.subActive ? ClientState.subPlan : "Free"));
        role = Fonts.body(ClientState.roleLabel);
        titleW = f.width(title);
        verW = f.width(ver);
        nameW = f.width(name);
        planW = f.width(plan);
        roleW = f.width(role);
        int row1 = 12 + 5 + titleW + (showVer ? 6 + verW + 8 : 0);
        int row2 = 12 + 7 + nameW + 6 + (planW + 10) + 4 + (roleW + 8);
        int content = Math.max(row1, profile ? row2 : 0);
        w = 9 + content + 8;
        h = profile ? 6 + 12 + 5 + 12 + 6 : 6 + 12 + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        int acc = Colors.themeAccent();
        HudManager.glassAccent(g, 0, 0, w, h, acc);
        int lx = 9;
        int y1 = 6;
        // бренд-марка
        Draw.roundRect(g, lx, y1 - 1, 12, 12, 3, acc);
        Component e = Fonts.display("E");
        g.drawString(f, e, lx + (12 - f.width(e)) / 2, y1 + 1, 0xFFFFFFFF, false);
        int tx = lx + 12 + 5;
        g.drawString(f, title, tx, y1 + 1, 0xFFFFFFFF, true);
        if (showVer) {
            int vx = tx + titleW + 6;
            Draw.roundRect(g, vx, y1, verW + 8, 11, 3, acc);
            g.drawString(f, ver, vx + 4, y1 + 2, 0xFFFFFFFF, false);
        }
        if (profile) {
            int y2 = y1 + 12 + 5;
            Draw.roundRect(g, lx, y2, 12, 12, 6, 0xFF46A171);
            Draw.roundRect(g, lx, y2 + 6, 12, 6, 6, 0xFF2F7350);
            String nm = ClientState.displayName.isEmpty()
                    ? (ClientState.username.isEmpty() ? "P" : ClientState.username.substring(0, 1))
                    : ClientState.displayName.substring(0, 1);
            Component ini = Fonts.body(nm.toUpperCase());
            g.drawString(f, ini, lx + (12 - f.width(ini)) / 2, y2 + 2, 0xFFFFFFFF, false);
            int nx = lx + 12 + 3;
            // онлайн-точка
            Draw.roundRect(g, nx, y2 + 4, 3, 3, 1, 0xFF3FB950);
            int nameX = nx + 4;
            g.drawString(f, name, nameX, y2 + 2, 0xFFFFFFFF, false);
            int bx = nameX + nameW + 6;
            Draw.roundRect(g, bx, y2, planW + 10, 11, 5, acc);
            g.drawString(f, plan, bx + 5, y2 + 2, 0xFFFFFFFF, false);
            int rx = bx + planW + 10 + 4;
            Draw.roundRect(g, rx, y2, roleW + 8, 11, 5, Colors.withAlpha(acc, 0x33));
            g.drawString(f, role, rx + 4, y2 + 2, acc, false);
        }
    }
}
