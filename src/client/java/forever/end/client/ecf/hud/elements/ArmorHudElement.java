package forever.end.client.ecf.hud.elements;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.ui.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Броня + прочность каждого предмета — порт .hud-armor. Низ по центру. */
public class ArmorHudElement extends HudElement {
    private static final int PITCH = 20;

    public ArmorHudElement() { super("armor", "Armor HUD", "Armor HUD", Anchor.BC, 0, -6); }

    private List<ItemStack> stacks(Minecraft mc) {
        List<ItemStack> list = new ArrayList<>();
        if (mc.player == null) return list;
        var inv = mc.player.getInventory();
        for (int i = 3; i >= 0; i--) {
            ItemStack s = inv.getArmor(i);
            if (!s.isEmpty()) list.add(s);
        }
        if (opt("\u0420\u0443\u043a\u0430", true)) {
            ItemStack hnd = mc.player.getMainHandItem();
            if (!hnd.isEmpty()) list.add(hnd);
        }
        return list;
    }

    @Override
    protected void layout(Minecraft mc) {
        int n = stacks(mc).size();
        this.w = Math.max(1, n * PITCH + 8);
        this.h = opt("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c", true) ? 28 : 24;
    }

    @Override
    protected void draw(GuiGraphics g, Minecraft mc, float partial, boolean editor) {
        List<ItemStack> list = stacks(mc);
        if (list.isEmpty()) return;
        boolean dur = opt("\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c", true);
        HudManager.card(g, 0, 0, w, h);
        int x = 6;
        for (ItemStack s : list) {
            g.renderItem(s, x, 5);
            if (dur && s.isDamageableItem()) {
                int max = s.getMaxDamage();
                float frac = max > 0 ? (float) (max - s.getDamageValue()) / max : 1f;
                Draw.roundRect(g, x, 23, 16, 3, 1, HudManager.TRACK);
                int c = frac > 0.3f ? 0xFF46A171 : HudManager.accent();
                Draw.roundRect(g, x, 23, Math.max(1, Math.round(16 * frac)), 3, 1, c);
            }
            x += PITCH;
        }
    }
}
