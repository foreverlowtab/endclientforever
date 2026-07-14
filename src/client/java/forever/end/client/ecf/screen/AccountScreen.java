package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Личный кабинет (открывается кликом по мини-профилю в главном меню). */
public class AccountScreen extends EcfScreen {
    private final Screen parent;

    public AccountScreen(Screen parent) {
        super(Component.literal("Личный кабинет"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cw = 300, ch = 220, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        int bx = cx + 20, bw = cw - 40;
        addRenderableWidget(Button.builder(Component.literal("↓ Скачать клиент"),
                b -> Util.getPlatform().openUri("https://endclient.fun")).bounds(bx, cy + 150, bw, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Профиль на сайте"),
                b -> Util.getPlatform().openUri("https://endclient.fun/profile.php?u=" + ClientState.username))
                .bounds(bx, cy + 172, bw, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Выйти из аккаунта"), b -> {
            ClientState.logout();
            this.minecraft.setScreen(new AuthScreen());
        }).bounds(bx, cy + 194, bw, 18).build());
        addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .bounds(cx + cw - 22, cy + 8, 16, 16).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0x9E000000);
        Theme t = theme();
        int cw = 300, ch = 220, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        Draw.panel(g, cx, cy, cw, ch, t.panel, t.border());
        String name = ClientState.username.isEmpty() ? "Player" : ClientState.username;
        Draw.rect(g, cx + 20, cy + 20, 40, 40, 0xFF46A171);
        g.drawString(this.font, name.substring(0, 1).toUpperCase(), cx + 36, cy + 36, 0xFFFFFFFF, false);
        g.drawString(this.font, name + "  [" + ClientState.role + "]", cx + 70, cy + 26, t.text, false);
        g.drawString(this.font, "@" + name, cx + 70, cy + 40, t.muted, false);
        Draw.panel(g, cx + 20, cy + 68, cw - 40, 22, t.panel2, t.border());
        g.drawString(this.font, "* Forever Lite  —  подписка активна · навсегда", cx + 28, cy + 75, t.accent, false);
        int sw = (cw - 40 - 20) / 3;
        drawStat(g, t, cx + 20, cy + 100, sw, "0.6-test", "Версия");
        drawStat(g, t, cx + 20 + sw + 10, cy + 100, sw, "1.21.4", "Minecraft");
        drawStat(g, t, cx + 20 + (sw + 10) * 2, cy + 100, sw, String.valueOf(Modules.enabledCount()), "Модулей вкл.");
        super.render(g, mx, my, pt);
    }

    private void drawStat(GuiGraphics g, Theme t, int x, int y, int w, String n, String l) {
        Draw.panel(g, x, y, w, 36, t.panel2, t.border());
        g.drawCenteredString(this.font, n, x + w / 2, y + 8, t.text);
        g.drawCenteredString(this.font, l, x + w / 2, y + 22, t.muted);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
