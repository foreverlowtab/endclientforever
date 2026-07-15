package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.EcfConfig;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.net.EndApi;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Обязательный экран авторизации — реальный вход через API сайта (дизайн 1:1 с auth-card). */
public class AuthScreen extends EcfScreen {
    private EditBox userBox;
    private EditBox passBox;
    private UiButton loginBtn;
    private String error = "";
    private String info = "";
    private boolean busy = false;

    // геометрия карточки
    private int cw = 320, ch = 268, cx, cy, pad = 24;

    public AuthScreen() {
        super(Component.literal("Авторизация"));
    }

    @Override
    protected void init() {
        addThemeToggle();
        cx = (this.width - cw) / 2;
        cy = (this.height - ch) / 2;
        int fx = cx + pad, fw = cw - pad * 2;

        userBox = new EditBox(this.font, fx + 8, cy + 150, fw - 16, 12, Component.literal("user"));
        userBox.setHint(Component.literal("ForeverLowTab"));
        userBox.setMaxLength(48);
        userBox.setBordered(false);
        userBox.setTextColor(theme().text);
        userBox.setValue(ClientState.username);
        addRenderableWidget(userBox);

        passBox = new EditBox(this.font, fx + 8, cy + 194, fw - 16, 12, Component.literal("pass"));
        passBox.setHint(Component.literal("••••••••"));
        passBox.setMaxLength(64);
        passBox.setBordered(false);
        passBox.setTextColor(theme().text);
        passBox.setFormatter((s, i) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY));
        addRenderableWidget(passBox);

        loginBtn = new UiButton(fx, cy + 216, fw, 24,
                Fonts.body("Войти и запустить →"), UiButton.Style.PRIMARY, this::tryLogin);
        addRenderableWidget(loginBtn);
        this.setInitialFocus(userBox);

        // Тихое восстановление сессии по сохранённому токену.
        if (!ClientState.authed && ClientState.token != null && !ClientState.token.isEmpty()) {
            busy = true;
            info = "Восстановление сессии…";
            setBusy(true);
            String tok = ClientState.token;
            EndApi.me(tok, r -> {
                busy = false;
                setBusy(false);
                info = "";
                if (r.ok && r.data != null) {
                    ClientState.applyLogin(tok, r.data);
                    ClientState.event("launch", "Автовход по сохранённой сессии");
                    if (this.minecraft != null) this.minecraft.setScreen(new MainMenuScreen());
                } else {
                    ClientState.token = "";
                    EcfConfig.save();
                }
            });
        }
    }

    private void setBusy(boolean b) {
        if (loginBtn != null) {
            loginBtn.active = !b;
            loginBtn.setMessage(Fonts.body(b ? "Проверка…" : "Войти и запустить →"));
        }
    }

    private void tryLogin() {
        if (busy) return;
        String u = userBox.getValue().trim();
        String p = passBox.getValue();
        if (u.isEmpty() || p.isEmpty()) {
            error = "Введите имя пользователя и пароль";
            return;
        }
        error = "";
        busy = true;
        setBusy(true);
        EndApi.login(u, p, r -> {
            busy = false;
            setBusy(false);
            if (r.ok && r.data != null && r.data.has("token")) {
                ClientState.applyLogin(r.data.get("token").getAsString(), r.data);
                ClientState.event("launch", "Главное меню открыто");
                if (this.minecraft != null) this.minecraft.setScreen(new MainMenuScreen());
            } else {
                error = r.message.isEmpty() ? "Неверный логин или пароль" : r.message;
            }
        });
    }

    @Override
    protected void renderBehind(GuiGraphics g, int mx, int my, float pt) {
        Theme t = theme();
        int bx = cx;

        // карточка
        Draw.roundRect(g, bx, cy + 6, cw, ch, 16, 0x33000000);
        Draw.roundRectBorder(g, bx, cy, cw, ch, 16, t.panel, t.border());

        // логотип
        Draw.roundRect(g, bx + pad, cy + 22, 40, 40, 12, t.accent);
        drawBig(g, Fonts.display("E"), bx + pad + 20, cy + 34, 0xFFFFFFFF, 2.0f);

        drawBig(g, Fonts.display("Авторизация"), bx + pad, cy + 72, t.text, 1.7f);
        g.drawString(this.font, Fonts.body("Войдите, чтобы запустить End Client Forever"), bx + pad, cy + 92, t.muted, false);

        // lock-note
        int lnW = cw - pad * 2;
        Draw.roundRect(g, bx + pad, cy + 108, lnW, 18, 9, t.accentSoft());
        Draw.roundRect(g, bx + pad + 8, cy + 114, 6, 6, 2, t.accent);
        g.drawString(this.font, Fonts.body("Вход обязателен — клиент не запустится без входа"), bx + pad + 20, cy + 114, t.accent, false);

        // поля
        int fx = bx + pad, fw = cw - pad * 2;
        g.drawString(this.font, Fonts.body("Имя пользователя или e-mail"), fx, cy + 136, t.muted, false);
        Draw.roundRectBorder(g, fx, cy + 146, fw, 20, 6, t.panel2,
                userBox != null && userBox.isFocused() ? t.accent : t.border());
        g.drawString(this.font, Fonts.body("Пароль"), fx, cy + 180, t.muted, false);
        Draw.roundRectBorder(g, fx, cy + 190, fw, 20, 6, t.panel2,
                passBox != null && passBox.isFocused() ? t.accent : t.border());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        Theme t = theme();
        if (!info.isEmpty()) {
            g.drawCenteredString(this.font, Fonts.body(info), cx + cw / 2, cy + 248, t.muted);
        } else if (!error.isEmpty()) {
            g.drawCenteredString(this.font, Fonts.body(error), cx + cw / 2, cy + 248, t.accent);
        }
        g.drawCenteredString(this.font, Fonts.body("v0.6-test · Minecraft 1.21.4 · Fabric"), cx + cw / 2, cy + ch - 16, t.muted);
    }

    private void drawBig(GuiGraphics g, Component c, int cxp, int cyp, int color, float scale) {
        g.pose().pushPose();
        g.pose().translate(cxp, cyp, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(this.font, c, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
