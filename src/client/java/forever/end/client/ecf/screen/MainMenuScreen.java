package forever.end.client.ecf.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Главное меню клиента (заменяет ванильный TitleScreen после авторизации). */
public class MainMenuScreen extends EcfScreen {
    private int chipX, chipY, chipW, chipH;

    public MainMenuScreen() {
        super(Component.literal("End Client Forever"));
    }

    @Override
    protected void init() {
        addThemeToggle();
        int bx = 60, bw = 200, bh = 20, gap = 6;
        int by = this.height / 2 - 10;
        addRenderableWidget(Button.builder(Component.literal("Одиночная игра"), b -> {
            ClientState.event("join_world", "Открыт выбор мира");
            this.minecraft.setScreen(new SelectWorldScreen(this));
        }).bounds(bx, by, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Сетевая игра"), b -> {
            ClientState.event("join_server", "Открыт список серверов");
            this.minecraft.setScreen(new JoinMultiplayerScreen(this));
        }).bounds(bx, by + (bh + gap), bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("ClickGUI  [R-Shift]"), b -> {
            ClientState.event("open_clickgui", "Открыт из главного меню");
            this.minecraft.setScreen(new ClickGuiScreen(this));
        }).bounds(bx, by + (bh + gap) * 2, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Настройки"),
                b -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(bx, by + (bh + gap) * 3, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Выход"),
                b -> this.minecraft.stop()).bounds(bx, by + (bh + gap) * 4, bw, bh).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        g.fill(0, 0, this.width, this.height, 0x88000000);
        Theme t = theme();
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(60, this.height / 2 - 90, 0);
        pose.scale(3.0f, 3.0f, 1.0f);
        g.drawString(this.font, "END CLIENT", 0, 0, 0xFFFFFFFF, true);
        g.drawString(this.font, "FOREVER", 0, 12, t.accent, true);
        pose.popPose();
        int badgeY = this.height / 2 - 34;
        Draw.rect(g, 60, badgeY, 130, 14, t.accent);
        g.drawString(this.font, "* Клиент активен", 66, badgeY + 3, 0xFFFFFFFF, false);
        brand(g);
        g.drawString(this.font, "End Client Forever 0.6-test · MC 1.21.4 · Fabric", 12, this.height - 16, 0xFFDDDDDD, true);
        String name = ClientState.username.isEmpty() ? "Player" : ClientState.username;
        chipW = 150;
        chipH = 30;
        chipX = this.width - 12 - chipW;
        chipY = this.height - 12 - chipH;
        Draw.panel(g, chipX, chipY, chipW, chipH, t.panel, t.border());
        Draw.rect(g, chipX + 6, chipY + 6, 18, 18, 0xFF46A171);
        g.drawString(this.font, name.substring(0, 1).toUpperCase(), chipX + 12, chipY + 11, 0xFFFFFFFF, false);
        g.drawString(this.font, name, chipX + 30, chipY + 7, t.text, false);
        g.drawString(this.font, ClientState.roleLabel, chipX + 30, chipY + 18, t.accent, false);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && mx >= chipX && mx <= chipX + chipW && my >= chipY && my <= chipY + chipH) {
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
