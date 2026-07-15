package forever.end.client.ecf.hud.elements;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Armor HUD: броня (шлем→ботинки) + предмет в руке, с полосами прочности. */
public class ArmorHudElement extends HudElement {
    private static final int SLOT = 18;
    private boolean durability, hand;
    private final List<ItemStack> items = new ArrayList<>();

    public ArmorHudElement() { super("armor", "Armor HUD", "Armor HUD", 0.42f, 0.84f); }

    @Override
    protected void layout(Minecraft mc) {
        durability = opt("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c", true);
        hand = opt("\u0420\u0443\u043a\u0430", true);
        items.clear();
        if (mc.player != null) {
            for (int i = 3; i >= 0; i--) {
                ItemStack s = mc.player.getInventory().getArmor(i);
                if (!s.isEmpty()) items.add(s);
            }
            if (hand) {
                ItemStack held = mc.player.getMainHandItem();
                if (!held.isEmpty()) items.add(held);
            }
        }
        int n = editing ? Math.max(items.size(), 4) : items.size();
        w = n == 0 ? 0 : 6 + n * SLOT + 6;
        h = n == 0 ? 0 : 6 + 16 + (durability ? 4 : 0) + 6;
        if (editing && n == 0) { w = 90; h = 28; }
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        Font f = mc.font;
        if (items.isEmpty()) {
            if (editor) {
                HudManager.glass(g, 0, 0, Math.max(w, 90), Math.max(h, 28));
                g.drawString(f, Fonts.body("Armor HUD"), 8, 10, HudManager.MUTED, false);
            }
            return;
        }
        HudManager.glass(g, 0, 0, w, h);
        int x = 6, y = 6;
        for (ItemStack s : items) {
            g.renderItem(s, x, y);
            g.renderItemDecorations(f, s, x, y);
            if (durability && s.isBarVisible()) {
                int bw = s.getBarWidth();
                int col = 0xFF000000 | s.getBarColor();
                int by = y + 16 + 1;
                g.fill(x, by, x + 16, by + 2, 0x80000000);
                g.fill(x, by, x + bw + 1, by + 2, col);
            }
            x += SLOT;
        }
    }
}
