package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.net.EndApi;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Личный кабинет — модальное окно (дизайн 1:1 с acc-card). */
public class AccountScreen extends EcfScreen {
    private final Screen parent;
    private UiButton downloadBtn;
    private String status = "";
    private boolean refreshing = false;

    private int cw = 380, ch = 300, cx, cy, pad = 24;

    public AccountScreen(Screen parent) {
        super(Component.literal("Личный кабинет"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cx = (this.width - cw) / 2;
        cy = (this.height - ch) / 2;
        int bx = cx + pad, bw = cw - pad * 2;

        downloadBtn = new UiButton(bx, cy + 196, bw, 26, Component.literal("↓ Скачать клиент"),
                UiButton.Style.PRIMARY, this::onDownload);
        downloadBtn.active = ClientState.canDownload;
        addRenderableWidget(downloadBtn);

        int half = (bw - 8) / 2;
        addRenderableWidget(new UiButton(bx, cy + 228, half, 22, Component.literal("Профиль на сайте"),
                UiButton.Style.GHOST, () -> Util.getPlatform().openUri("https://endclient.fun/profile.php?u=" + ClientState.username)));
        addRenderableWidget(new UiButton(bx + half + 8, cy + 228, half, 22, Component.literal("↻ Обновить"),
                UiButton.Style.GHOST, this::refresh));
        addRenderableWidget(new UiButton(bx, cy + 256, bw, 22, Component.literal("Выйти из аккаунта"),
                UiButton.Style.GHOST, () -> {
                    ClientState.logout();
                    this.minecraft.setScreen(new AuthScreen());
                }));
        addRenderableWidget(new UiButton(cx + cw - 12 - 24, cy + 12, 24, 24, Component.literal("✕"),
                UiButton.Style.ICON_CIRCLE, this::onClose));
    }

    private void onDownload() {
        if (!ClientState.canDownload) {
            status = "Нужна активная подписка Forever Lite";
            return;
        }
        ClientState.event("download", "Кнопка скачивания в кабинете");
        Util.getPlatform().openUri(ClientState.downloadUrl);
        status = "Открыли страницу скачивания в браузере";
    }

    private void refresh() {
        if (refreshing || ClientState.token.isEmpty()) return;
        refreshing = true;
        status = "Обновление…";
        EndApi.me(ClientState.token, r -> {
            refreshing = false;
            if (r.ok && r.data != null) {
                ClientState.applyLogin(ClientState.token, r.data);
                if (downloadBtn != null) downloadBtn.active = ClientState.canDownload;
                status = "Обновлено";
            } else {
                status = r.message.isEmpty() ? "Не удалось обновить" : r.message;
            }
        });
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderPanorama(g);
        g.fill(0, 0, this.width, this.height, 0x88000000);
        Theme t = theme();

        Draw.roundRect(g, cx, cy + 6, cw, ch, 16, 0x40000000);
        Draw.roundRectBorder(g, cx, cy, cw, ch, 16, t.panel, t.border());

        String name = ClientState.displayName.isEmpty()
                ? (ClientState.username.isEmpty() ? "Player" : ClientState.username)
                : ClientState.displayName;

        // шапка: аватар + имя + роль
        int avS = 52, avX = cx + pad, avY = cy + pad;
        Draw.roundRect(g, avX, avY, avS, avS, 16, t.avatarA);
        Draw.roundRect(g, avX, avY + avS / 2, avS, avS / 2, 16, t.avatarB);
        drawBig(g, name.substring(0, 1).toUpperCase(), avX + avS / 2 - 6, avY + 14, 0xFFFFFFFF, 2.2f);
        int nx = avX + avS + 14;
        drawBig(g, name, nx, cy + pad + 6, t.text, 1.5f);
        int rbW = this.font.width(ClientState.roleLabel) + 12;
        Draw.roundRect(g, nx, cy + pad + 22, rbW, 13, 6, t.accentSoft());
        g.drawString(this.font, ClientState.roleLabel, nx + 6, cy + pad + 24, t.accent, false);
        g.drawString(this.font, "@" + ClientState.username, nx, cy + pad + 40, t.muted, false);

        // подписка
        int sy = cy + 96, sw = cw - pad * 2;
        Draw.roundRectBorder(g, cx + pad, sy, sw, 26, 8, t.panel2, t.border());
        String badge = "◆ " + ClientState.subPlan;
        int sbW = this.font.width(badge) + 16;
        Draw.roundRectBorder(g, cx + pad + 8, sy + 6, sbW, 14, 7,
                ClientState.subActive ? t.accentSoft() : t.panel2, t.border());
        g.drawString(this.font, badge, cx + pad + 16, sy + 9, ClientState.subActive ? t.accent : t.muted, false);
        g.drawString(this.font, ClientState.subLabel(), cx + pad + 8 + sbW + 10, sy + 9, t.muted, false);

        // статы (3)
        int stY = cy + 132, gap = 10;
        int stW = (sw - gap * 2) / 3;
        drawStat(g, t, cx + pad, stY, stW, "0.6-test", "Версия");
        drawStat(g, t, cx + pad + stW + gap, stY, stW, "1.21.4", "Minecraft");
        drawStat(g, t, cx + pad + (stW + gap) * 2, stY, stW, String.valueOf(Modules.enabledCount()), "Модулей вкл.");

        if (!ClientState.canDownload) {
            g.drawString(this.font, "[!] Скачивание доступно по подписке", cx + pad, cy + 184, t.muted, false);
        }

        super.render(g, mx, my, pt);
        if (!status.isEmpty()) {
            g.drawCenteredString(this.font, status, cx + cw / 2, cy + ch - 14, t.accent);
        }
    }

    private void drawStat(GuiGraphics g, Theme t, int x, int y, int w, String n, String l) {
        Draw.roundRectBorder(g, x, y, w, 40, 8, t.panel2, t.border());
        g.drawCenteredString(this.font, n, x + w / 2, y + 10, t.text);
        g.drawCenteredString(this.font, l, x + w / 2, y + 24, t.muted);
    }

    private void drawBig(GuiGraphics g, String s, int x, int y, int color, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(this.font, s, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
