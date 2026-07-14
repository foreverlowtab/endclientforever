package forever.end.client.ecf.screen;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import forever.end.client.ecf.ui.Draw;
import forever.end.client.ecf.ui.UiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Базовый экран: панорама-фон, верхняя панель (бренд + переключатель тем).
 * Важно: весь кастомный фон рисуется в renderBackground(), чтобы кнопки-виджеты ложились СВЕРХУ. */
public abstract class EcfScreen extends Screen {
    private UiButton segRed;
    private UiButton segClaude;

    protected EcfScreen(Component title) {
        super(title);
    }

    protected Theme theme() {
        return ClientState.theme;
    }

    /** Сегментированный переключатель тем справа в топбаре. */
    protected void addThemeToggle() {
        int segW = 46, segH = 16, gap = 2, pad = 12, y = 10;
        int x2 = this.width - pad - segW;
        int x1 = x2 - gap - segW;
        Theme cur = theme();
        segRed = new UiButton(x1, y, segW, segH, Component.literal("Red"),
                cur == Theme.RED ? UiButton.Style.SEG_ACTIVE : UiButton.Style.SEG_INACTIVE,
                () -> switchTheme(Theme.RED));
        segClaude = new UiButton(x2, y, segW, segH, Component.literal("Claude"),
                cur == Theme.CLAUDE ? UiButton.Style.SEG_ACTIVE : UiButton.Style.SEG_INACTIVE,
                () -> switchTheme(Theme.CLAUDE));
        addRenderableWidget(segRed);
        addRenderableWidget(segClaude);
    }

    private void switchTheme(Theme t) {
        ClientState.setTheme(t);
        this.rebuildWidgets();
    }

    /** Фон рисуется здесь — ПОД виджетами (super.render() вызывает этот метод перед отрисовкой кнопок). */
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        renderScene(g, mx, my, pt);
    }

    /** Сцена под виджетами. Модальные экраны могут переопределить. */
    protected void renderScene(GuiGraphics g, int mx, int my, float pt) {
        renderPanorama(g);
        renderTopBar(g);
        renderBehind(g, mx, my, pt);
    }

    /** Кастомные панели экрана — рисуются под кнопками. */
    protected void renderBehind(GuiGraphics g, int mx, int my, float pt) {}

    /** Панорама-фон (аппроксимация градиентов + сетка + скрим из HTML). */
    protected void renderPanorama(GuiGraphics g) {
        Theme t = theme();
        int w = this.width, h = this.height;
        g.fillGradient(0, 0, w, h, t.pano3, 0xFF08090C);
        g.fillGradient(0, 0, w, (int) (h * 0.62f), Draw.alpha(t.pano2, 0x3A), 0x00000000);
        g.fillGradient(0, (int) (h * 0.40f), w, h, 0x00000000, Draw.alpha(t.pano1, 0x3A));
        int grid = 0x0DFFFFFF;
        for (int gx = 0; gx < w; gx += 24) g.fill(gx, 0, gx + 1, h, grid);
        for (int gy = 0; gy < h; gy += 24) g.fill(0, gy, w, gy + 1, grid);
        g.fillGradient(0, 0, w, h, 0x00000000, 0x59000000);
        g.fillGradient(0, 0, w, (int) (h * 0.28f), 0x40000000, 0x00000000);
    }

    /** Верхняя панель: бренд слева, контейнер переключателя тем справа. */
    protected void renderTopBar(GuiGraphics g) {
        int mx = 12, my = 8, ms = 20;
        Draw.roundRect(g, mx, my, ms, ms, 6, theme().accent);
        g.drawString(this.font, "E", mx + (ms - this.font.width("E")) / 2, my + ms / 2 - 4, 0xFFFFFFFF, false);
        g.drawString(this.font, "End Client Forever", mx + ms + 8, my + ms / 2 - 4, 0xFFFFFFFF, true);
        if (segRed != null && segClaude != null) {
            int cx = segRed.getX() - 4;
            int cy = segRed.getY() - 2;
            int cw = (segClaude.getX() + segClaude.getWidth()) - segRed.getX() + 8;
            int ch = segRed.getHeight() + 4;
            Draw.roundRect(g, cx, cy, cw, ch, ch / 2, 0x30FFFFFF);
        }
    }
}
