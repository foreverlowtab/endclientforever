package forever.end.client.ecf.hud.elements;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Colors;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/** Координаты: XYZ + направление (опционально биом). */
public class CoordinatesHud extends HudElement {
    private String xs = "0", ys = "0", zs = "0", facing = "";
    private String biomeStr = "";
    private boolean showFacing, showBiome;
    private int rowW;

    public CoordinatesHud() { super("coordinates", "Coordinates", "Coordinates", 0.008f, 0.86f); }

    private static String dirRu(Direction d) {
        return switch (d) {
            case NORTH -> "\u0421\u0435\u0432\u0435\u0440 (-Z)";
            case SOUTH -> "\u042e\u0433 (+Z)";
            case WEST -> "\u0417\u0430\u043f\u0430\u0434 (-X)";
            case EAST -> "\u0412\u043e\u0441\u0442\u043e\u043a (+X)";
            default -> d.getName();
        };
    }

    @Override
    protected void layout(Minecraft mc) {
        Font f = mc.font;
        showFacing = opt("\u041d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435", true);
        showBiome = opt("\u0411\u0438\u043e\u043c", false);
        if (mc.player != null) {
            xs = String.valueOf((int) Math.floor(mc.player.getX()));
            ys = String.valueOf((int) Math.floor(mc.player.getY()));
            zs = String.valueOf((int) Math.floor(mc.player.getZ()));
            facing = dirRu(mc.player.getDirection());
            if (showBiome && mc.level != null) {
                BlockPos p = mc.player.blockPosition();
                biomeStr = mc.level.getBiome(p).unwrapKey().map(k -> k.location().getPath()).orElse("?");
            }
        }
        int coordW = f.width(Fonts.body("X " + xs)) + 8 + f.width(Fonts.body("Y " + ys)) + 8 + f.width(Fonts.body("Z " + zs));
        int facW = showFacing ? f.width(Fonts.body(facing)) : 0;
        int bioW = showBiome ? f.width(Fonts.body(biomeStr)) : 0;
        rowW = Math.max(coordW, Math.max(facW, bioW));
        w = 8 + rowW + 8;
        h = 6 + 9 + (showFacing ? 3 + 8 : 0) + (showBiome ? 2 + 8 : 0) + 6;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        int acc = Colors.themeAccent();
        HudManager.glass(g, 0, 0, w, h);
        int x = 8, y = 6;
        x = axis(g, f, x, y, "X", xs, acc);
        x += 8;
        x = axis(g, f, x, y, "Y", ys, acc);
        x += 8;
        axis(g, f, x, y, "Z", zs, acc);
        int yy = y + 9;
        if (showFacing) {
            yy += 3;
            g.drawString(f, Fonts.body(facing), 8, yy, HudManager.MUTED, false);
            yy += 8;
        }
        if (showBiome) {
            yy += 2;
            g.drawString(f, Fonts.body(biomeStr), 8, yy, HudManager.MUTED, false);
        }
    }

    private int axis(GuiGraphics g, Font f, int x, int y, String ax, String val, int acc) {
        Component a = Fonts.body(ax);
        g.drawString(f, a, x, y, acc, false);
        int ax2 = x + f.width(a) + 3;
        Component v = Fonts.body(val);
        g.drawString(f, v, ax2, y, 0xFFFFFFFF, false);
        return ax2 + f.width(v);
    }
}
