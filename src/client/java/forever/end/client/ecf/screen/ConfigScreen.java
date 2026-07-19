package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.ConfigManager;
import forever.end.client.ecf.net.EndApi;
import forever.end.client.ecf.ui.Fonts;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Экран системы конфигов: локально всё сохраняется автоматически, здесь — облако и сброс. */
public class ConfigScreen extends EcfScreen {
    private static final int OK = 0xFF41D18A;
    private static final int ERR = 0xFFE5484D;
    private static final int INFO = 0xFFC6CBD8;

    private final Screen parent;
    private String status = "";
    private int statusColor = INFO;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Конфиги"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addThemeToggle();
        int w = 268, h = 30, gap = 10;
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 34;

        addRenderableWidget(new UiButton(x, y, w, h, Fonts.body("Сохранить в облако"),
                UiButton.Style.MENU_PRIMARY, this::saveCloud).icon("☁"));
        addRenderableWidget(new UiButton(x, y + (h + gap), w, h, Fonts.body("Загрузить из облака"),
                UiButton.Style.MENU, this::loadCloud).icon("☁"));
        addRenderableWidget(new UiButton(x, y + (h + gap) * 2, w, h, Fonts.body("Сбросить (всё выключить)"),
                UiButton.Style.MENU, this::resetAll).icon("↺"));
        addRenderableWidget(new UiButton(x, y + (h + gap) * 3, w, h, Fonts.body("Назад"),
                UiButton.Style.MENU, this::onClose).icon("←"));
    }

    private void saveCloud() {
        ConfigManager.saveLocal();
        if (!ClientState.authed || ClientState.token.isEmpty()) { setStatus("Нужен вход в аккаунт", ERR); return; }
        setStatus("Сохранение…", INFO);
        EndApi.configSave(ClientState.token, ConfigManager.captureString(), r -> {
            if (r.ok) setStatus("Конфиг сохранён в облако", OK);
            else setStatus("Ошибка: " + (r.message.isEmpty() ? "не удалось" : r.message), ERR);
        });
        ClientState.event("config_save", "Сохранение конфига в облако");
    }

    private void loadCloud() {
        if (!ClientState.authed || ClientState.token.isEmpty()) { setStatus("Нужен вход в аккаунт", ERR); return; }
        setStatus("Загрузка…", INFO);
        EndApi.configLoad(ClientState.token, r -> {
            if (r.ok && r.data != null && r.data.has("data") && !r.data.get("data").isJsonNull()) {
                String d = r.data.get("data").getAsString();
                if (d == null || d.isEmpty()) { setStatus("В облаке пока пусто", INFO); return; }
                ConfigManager.applyString(d);
                this.rebuildWidgets();
                setStatus("Конфиг загружен из облака", OK);
            } else {
                setStatus(r.ok ? "В облаке пока пусто" : ("Ошибка: " + r.message), r.ok ? INFO : ERR);
            }
        });
        ClientState.event("config_load", "Загрузка конфига из облака");
    }

    private void resetAll() {
        ConfigManager.allOff();
        ConfigManager.saveLocal();
        setStatus("Все функции выключены", INFO);
    }

    private void setStatus(String s, int color) {
        this.status = s;
        this.statusColor = color;
    }

    @Override
    protected void renderBehind(GuiGraphics g, int mx, int my, float pt) {
        int cx = this.width / 2;
        int topY = this.height / 2 - 34;
        drawCenter(g, Fonts.display("Система конфигов"), cx, topY - 58, 0xFFFFFFFF);
        drawCenter(g, Fonts.body("Локально конфиг сохраняется сам — при каждом изменении и выходе из игры."), cx, topY - 38, INFO);
        drawCenter(g, Fonts.body("Облако переносит настройки на любой другой компьютер."), cx, topY - 26, INFO);
        if (!status.isEmpty()) {
            int by = topY + (30 + 10) * 4 + 6;
            drawCenter(g, Fonts.body(status), cx, by, statusColor);
        }
    }

    private void drawCenter(GuiGraphics g, Component c, int cx, int y, int color) {
        g.drawString(this.font, c, cx - this.font.width(c) / 2, y, color, true);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
