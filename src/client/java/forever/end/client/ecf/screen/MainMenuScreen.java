package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.Fonts;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Главное меню (дизайн 1:1 с HTML: бейдж, большой тайтл, меню-кнопки, мини-профиль). */
public class MainMenuScreen extends EcfScreen {
    private int chipX, chipY, chipW, chipH, chipClickW;

    public MainMenuScreen() {
        super(Component.literal("End Client Forever"));
    }

    @Override
    protected void init() {
        addThemeToggle();
        int lx = 40;
        int mw = 236, mh = 28, gap = 9;
        int by = this.height / 2 - 12;
        addRenderableWidget(new UiButton(lx, by, mw, mh, Fonts.body("Одиночная игра"),
                UiButton.Style.MENU, () -> {
                    ClientState.event("join_world", "Открыт выбор мира");
                    this.minecraft.setScreen(new SelectWorldScreen(this));
                }).icon("▶"));
        addRenderableWidget(new UiButton(lx, by + (mh + gap), mw, mh, Fonts.body("Сетевая игра"),
                UiButton.Style.MENU, () -> {
                    ClientState.event("join_server", "Открыт список серверов");
                    this.minecraft.setScreen(new JoinMultiplayerScreen(this));
                }).icon("▤"));
        addRenderableWidget(new UiButton(lx, by + (mh + gap) * 2, mw, mh, Fonts.body("ClickGUI"),
                UiButton.Style.MENU_PRIMARY, () -> {
                    ClientState.event("open_clickgui", "Открыт из главного меню");
                    this.minecraft.setScreen(new ClickGuiScreen(this));
                }).icon("◆").kbd("R-Shift"));
        addRenderableWidget(new UiButton(lx, by + (mh + gap) * 3, mw, mh, Fonts.body("Настройки"),
                UiButton.Style.MENU, () -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).icon("⚙"));
        addRenderableWidget(new UiButton(lx, by + (mh + gap) * 4, mw, mh, Fonts.body("Выход"),
                UiButton.Style.MENU, () -> this.minecraft.stop()).icon("⏻"));

        // Мини-профиль (user-chip) внизу справа.
        String name = displayName();
        int contentW = Math.max(this.font.width(Fonts.display(name)), this.font.width(Fonts.body(ClientState.roleLabel)) + 8);
        chipH = 40;
        chipW = 10 + 26 + 8 + contentW + 10 + 26 + 8;
        chipX = this.width - 14 - chipW;
        chipY = this.height - 14 - chipH;
        chipClickW = chipW - 26 - 8; // область открытия кабинета (без кнопки выхода)
        int lbX = chipX + chipW - 8 - 26;
        addRenderableWidget(new UiButton(lbX, chipY + (chipH - 26) / 2, 26, 26, Component.literal("⏻"),
                UiButton.Style.ICON_CIRCLE, () -> {
                    ClientState.logout();
                    this.minecraft.setScreen(new AuthScreen());
                }));
    }

    private String displayName() {
        if (!ClientState.displayName.isEmpty()) return ClientState.displayName;
        return ClientState.username.isEmpty() ? "Player" : ClientState.username;
    }

    @Override
    protected void renderBehind(GuiGraphics g, int mx, int my, float pt) {
        Theme t = theme();
        int lx = 40;

        // вотермарк
        Component wm = Fonts.display("End Client Forever");
        g.drawString(this.font, wm, lx, 40, 0xFFFFFFFF, true);
        int wmW = this.font.width(wm);
        Component ver = Fonts.body("v0.6-test");
        Draw.roundRect(g, lx + wmW + 6, 38, this.font.width(ver) + 8, 12, 3, 0xFFFFFFFF);
        g.drawString(this.font, ver, lx + wmW + 10, 40, t.accent, false);

        // бейдж "Клиент активен"
        int badgeY = this.height / 2 - 96;
        Component badge = Fonts.body("Клиент активен");
        int badgeW = this.font.width(badge) + 26;
        Draw.roundRect(g, lx, badgeY, badgeW, 16, 8, t.accent);
        Draw.roundRect(g, lx + 9, badgeY + 6, 4, 4, 2, 0xFFFFFFFF);
        g.drawString(this.font, badge, lx + 18, badgeY + 4, 0xFFFFFFFF, false);

        // тайтл (настоящий дисплейный шрифт)
        drawTitle(g, "END CLIENT", lx, this.height / 2 - 80, 0xFFFFFFFF, false);
        drawTitle(g, "FOREVER", lx, this.height / 2 - 50, 0xFFFFFFFF, true);

        // версия внизу слева
        g.drawString(this.font, Fonts.body("End Client Forever 0.6-test · MC 1.21.4 · Fabric"), lx, this.height - 18, 0xFFE8E8EC, true);

        // мини-профиль
        boolean chipHover = mx >= chipX && mx <= chipX + chipClickW && my >= chipY && my <= chipY + chipH;
        Draw.roundRect(g, chipX, chipY + 3, chipW, chipH, chipH / 2, 0x33000000);
        Draw.pillBorder(g, chipX, chipY, chipW, chipH, t.panel, chipHover ? t.accent : t.border());
        int avS = 26, avX = chipX + 10, avY = chipY + (chipH - avS) / 2;
        Draw.roundRect(g, avX, avY, avS, avS, avS / 2, t.avatarA);
        Draw.roundRect(g, avX, avY + avS / 2, avS, avS / 2, avS / 2, t.avatarB);
        String name = displayName();
        Component avl = Fonts.display(name.substring(0, 1).toUpperCase());
        g.drawString(this.font, avl, avX + (avS - this.font.width(avl)) / 2, avY + avS / 2 - 4, 0xFFFFFFFF, false);
        int tx = avX + avS + 8;
        g.drawString(this.font, Fonts.display(name), tx, chipY + 9, chipHover ? t.accent : t.text, false);
        Component role = Fonts.body(ClientState.roleLabel);
        int rbW = this.font.width(role) + 12;
        Draw.roundRect(g, tx, chipY + 22, rbW, 12, 6, t.accentSoft());
        g.drawString(this.font, role, tx + 6, chipY + 24, t.accent, false);
    }

    /** Большой заголовок дисплейным шрифтом; outline=true — контурный текст (как .l2 в HTML). */
    private void drawTitle(GuiGraphics g, String s, int x, int y, int color, boolean outline) {
        float sc = 3.0f;
        Component c = Fonts.display(s);
        if (outline) {
            int oc = 0xFFFFFFFF;
            int[][] off = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int[] d : off) drawScaled(g, c, x + d[0], y + d[1], sc, oc);
            drawScaled(g, c, x, y, sc, 0xFF12131A);
        } else {
            drawScaled(g, c, x, y, sc, color);
        }
    }

    /** Рисует Component с масштабом; смещение (x,y) — в экранных пикселях (тонкий контур). */
    private void drawScaled(GuiGraphics g, Component c, int x, int y, float sc, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(sc, sc, 1f);
        g.drawString(this.font, c, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && mx >= chipX && mx <= chipX + chipClickW && my >= chipY && my <= chipY + chipH) {
            ClientState.event("open_account", "Открыт личный кабинет");
            this.minecraft.setScreen(new AccountScreen(this));
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            ClientState.event("open_clickgui", "Открыт по R-Shift");
            this.minecraft.setScreen(new ClickGuiScreen(this));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
