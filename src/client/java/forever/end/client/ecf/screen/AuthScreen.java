package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Обязательный экран авторизации — всегда первый, обойти нельзя. */
public class AuthScreen extends EcfScreen {
    private EditBox userBox;
    private EditBox passBox;
    private String error = "";

    public AuthScreen() {
        super(Component.literal("Авторизация"));
    }

    @Override
    protected void init() {
        addThemeToggle();
        int cw = 300, ch = 232, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        int fx = cx + 24, fw = cw - 48;
        userBox = new EditBox(this.font, fx, cy + 116, fw, 20, Component.literal("user"));
        userBox.setHint(Component.literal("ForeverLowTab"));
        userBox.setMaxLength(48);
        addRenderableWidget(userBox);
        passBox = new EditBox(this.font, fx, cy + 152, fw, 20, Component.literal("pass"));
        passBox.setHint(Component.literal("******"));
        passBox.setMaxLength(64);
        passBox.setFormatter((s, i) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY));
        addRenderableWidget(passBox);
        addRenderableWidget(Button.builder(Component.literal("Войти и запустить →"), b -> tryLogin())
                .bounds(fx, cy + 180, fw, 20).build());
        this.setInitialFocus(userBox);
    }

    private void tryLogin() {
        String u = userBox.getValue().trim();
        String p = passBox.getValue().trim();
        if (u.isEmpty() || p.isEmpty()) {
            error = "Введите имя пользователя и пароль";
            return;
        }
        // Заглушка: реальная проверка через API сайта будет позже.
        ClientState.login(u.contains("@") ? u.substring(0, u.indexOf('@')) : u);
        this.minecraft.setScreen(new MainMenuScreen());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        scrim(g);
        Theme t = theme();
        int cw = 300, ch = 232, cx = (this.width - cw) / 2, cy = (this.height - ch) / 2;
        Draw.panel(g, cx, cy, cw, ch, t.panel, t.border());
        Draw.rect(g, cx + 24, cy + 18, 34, 34, t.accent);
        g.drawString(this.font, "E", cx + 38, cy + 31, 0xFFFFFFFF, false);
        g.drawString(this.font, "Авторизация", cx + 24, cy + 60, t.text, false);
        g.drawString(this.font, "Войдите, чтобы запустить End Client Forever", cx + 24, cy + 74, t.muted, false);
        Draw.rect(g, cx + 24, cy + 88, cw - 48, 14, t.accentSoft());
        g.drawString(this.font, "[!] Вход обязателен — клиент не запустится без входа", cx + 30, cy + 91, t.accent, false);
        g.drawString(this.font, "Имя пользователя или e-mail", cx + 24, cy + 106, t.muted, false);
        g.drawString(this.font, "Пароль", cx + 24, cy + 142, t.muted, false);
        super.render(g, mx, my, pt);
        if (!error.isEmpty()) {
            g.drawCenteredString(this.font, error, this.width / 2, cy + 206, t.accent);
        }
        g.drawCenteredString(this.font, "v0.6-test · Minecraft 1.21.4 · Fabric", this.width / 2, cy + ch + 10, 0xFFCCCCCC);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
