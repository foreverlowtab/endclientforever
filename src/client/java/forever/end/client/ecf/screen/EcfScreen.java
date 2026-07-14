package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Базовый экран с переключателем тем и общими хелперами. */
public abstract class EcfScreen extends Screen {
    protected EcfScreen(Component title) {
        super(title);
    }

    protected Theme theme() {
        return ClientState.theme;
    }

    protected void addThemeToggle() {
        int bw = 48, bh = 16, gap = 3, y = 8;
        int x = this.width - 8 - (bw * 2 + gap);
        Theme cur = theme();
        addRenderableWidget(Button.builder(Component.literal(cur == Theme.RED ? "[Red]" : "Red"),
                b -> switchTheme(Theme.RED)).bounds(x, y, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal(cur == Theme.CLAUDE ? "[Claude]" : "Claude"),
                b -> switchTheme(Theme.CLAUDE)).bounds(x + bw + gap, y, bw, bh).build());
    }

    private void switchTheme(Theme t) {
        ClientState.setTheme(t);
        this.rebuildWidgets();
    }

    protected void scrim(GuiGraphics g) {
        g.fill(0, 0, this.width, this.height, 0xB4000000);
    }

    protected void brand(GuiGraphics g) {
        g.drawString(this.font, "End Client Forever", 10, 12, 0xFFFFFFFF, true);
    }
}
