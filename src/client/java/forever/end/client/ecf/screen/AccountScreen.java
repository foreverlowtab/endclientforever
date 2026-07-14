package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.net.EndApi;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Личный кабинет (реальные данные с сайта: профиль, подписка, скачивание). */
public class AccountScreen extends EcfScreen {
    private final Screen parent;
    private Button downloadBtn;
    private String status = "";
    private boolean refreshing = false;

    public AccountScreen(Screen parent) {
        super(Component.literal("Личный кабинет"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cw = 320, ch = 244, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        int bx = cx + 20, bw = cw - 40;
        int half = (bw - 8) / 2;

        downloadBtn = Button.builder(Component.literal("↓ Скачать клиент"), b -> onDownload())
                .bounds(bx, cy + 158, bw, 18).build();
        downloadBtn.active = ClientState.canDownload;
        addRenderableWidget(downloadBtn);

        addRenderableWidget(Button.builder(Component.literal("Профиль на сайте"),
                b -> Util.getPlatform().openUri("https://endclient.fun/profile.php?u=" + ClientState.username))
                .bounds(bx, cy + 180, half, 18).build());
        addRenderableWidget(Button.builder(Component.literal("↻ Обновить"), b -> refresh())
                .bounds(bx + half + 8, cy + 180, half, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Выйти из аккаунта"), b -> {
            ClientState.logout();
            this.minecraft.setScreen(new AuthScreen());
        }).bounds(bx, cy + 202, bw, 18).build());
        addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .bounds(cx + cw - 22, cy + 8, 16, 16).build());
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
        this.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0x9E000000);
        Theme t = theme();
        int cw = 320, ch = 244, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        Draw.panel(g, cx, cy, cw, ch, t.panel, t.border());

        String name = ClientState.displayName.isEmpty()
                ? (ClientState.username.isEmpty() ? "Player" : ClientState.username)
                : ClientState.displayName;
        Draw.rect(g, cx + 20, cy + 20, 40, 40, parseColor(ClientState.avatarColor));
        g.drawString(this.font, name.substring(0, 1).toUpperCase(), cx + 36, cy + 36, 0xFFFFFFFF, false);
        g.drawString(this.font, name + "  [" + ClientState.roleLabel + "]", cx + 70, cy + 24, t.text, false);
        g.drawString(this.font, "@" + ClientState.username, cx + 70, cy + 38, t.muted, false);
        g.drawString(this.font, "С нами с " + ClientState.memberSince, cx + 70, cy + 50, t.muted, false);

        // Подписка
        Draw.panel(g, cx + 20, cy + 70, cw - 40, 22, t.panel2, t.border());
        int subColor = ClientState.subActive ? t.accent : 0xFFB9BEC7;
        g.drawString(this.font, (ClientState.subActive ? "* " : "· ") + ClientState.subLabel(), cx + 28, cy + 77, subColor, false);

        // Статы
        int sw = (cw - 40 - 20) / 3;
        drawStat(g, t, cx + 20, cy + 100, sw, "0.6-test", "Версия");
        drawStat(g, t, cx + 20 + sw + 10, cy + 100, sw, String.valueOf(ClientState.downloads), "Скачиваний");
        drawStat(g, t, cx + 20 + (sw + 10) * 2, cy + 100, sw, String.valueOf(Modules.enabledCount()), "Модулей вкл.");

        if (!ClientState.canDownload) {
            g.drawString(this.font, "[!] Скачивание доступно по подписке", cx + 20, cy + 144, t.muted, false);
        }

        super.render(g, mx, my, pt);
        if (!status.isEmpty()) {
            g.drawCenteredString(this.font, status, this.width / 2, cy + ch - 12, t.accent);
        }
    }

    private void drawStat(GuiGraphics g, Theme t, int x, int y, int w, String n, String l) {
        Draw.panel(g, x, y, w, 36, t.panel2, t.border());
        g.drawCenteredString(this.font, n, x + w / 2, y + 8, t.text);
        g.drawCenteredString(this.font, l, x + w / 2, y + 22, t.muted);
    }

    private int parseColor(String hex) {
        try {
            if (hex != null && hex.startsWith("#") && hex.length() == 7) {
                return 0xFF000000 | Integer.parseInt(hex.substring(1), 16);
            }
        } catch (Exception ignored) {
        }
        return 0xFF46A171;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
