package forever.end.client.ecf.hud.elements;

import java.util.Locale;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

/** Координаты XYZ + направление + биом — порт .hud-coords. Низ слева. */
public class CoordinatesHud extends HudElement {
    public CoordinatesHud() { super("coords", "Coordinates", "Coordinates", Anchor.BL, 6, -6); }

    @Override
    protected void layout(Minecraft mc) {
        boolean facing = opt("\u041d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435", true);
        boolean biome = opt("\u0411\u0438\u043e\u043c", false);
        this.w = 112;
        this.h = 5 + 30 + (facing ? 12 : 0) + (biome ? 11 : 0) + 4;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        boolean facing = opt("\u041d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435", true);
        boolean biome = opt("\u0411\u0438\u043e\u043c", false);
        HudManager.card(g, 0, 0, w, h);
        Font f = mc.font;
        var p = mc.player;
        line(g, f, "X", fmt(p.getX()), 5);
        line(g, f, "Y", fmt(p.getY()), 15);
        line(g, f, "Z", fmt(p.getZ()), 25);
        int y = 37;
        if (facing) {
            HudManager.text(g, f, Fonts.body("Facing: " + dir(p.getDirection())), 8, y, 0.85f, HudManager.MUTED);
            y += 12;
        }
        if (biome) {
            HudManager.text(g, f, Fonts.body(biomeName(mc)), 8, y, 0.85f, HudManager.MUTED);
        }
    }

    private void line(GuiGraphics g, Font f, String ax, String val, int y) {
        HudManager.text(g, f, Fonts.grotesk(ax), 8, y, 0.9f, HudManager.accent2());
        HudManager.text(g, f, Fonts.body(val), 20, y, 0.9f, HudManager.TXT);
    }

    private static String fmt(double v) { return String.format(Locale.US, "%.2f", v); }

    private static String dir(Direction d) {
        return switch (d) {
            case SOUTH -> "South (+Z)";
            case WEST -> "West (-X)";
            case NORTH -> "North (-Z)";
            case EAST -> "East (+X)";
            default -> d.getName();
        };
    }

    private static String biomeName(Minecraft mc) {
        try {
            return mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("\u2014");
        } catch (Exception e) {
            return "\u2014";
        }
    }
}
