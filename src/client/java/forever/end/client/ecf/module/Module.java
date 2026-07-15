package forever.end.client.ecf.module;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.module.setting.Setting;
import forever.end.client.ecf.ui.Colors;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Визуальный модуль клиента. Хранит имя/клавишу/состояние + список настроек
 * и функциональные хуки (мир, HUD, тик, вкл/выкл). Модули без хуков остаются
 * простыми переключателями (как раньше).
 */
public class Module {
    public final String name;
    public final String key;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    public interface WorldRender { void render(WorldRenderContext ctx, Module m); }
    public interface HudRender { void render(GuiGraphics g, float partial, Module m); }
    public interface Tick { void tick(Minecraft mc, Module m); }
    public interface Toggle { void run(Module m); }

    private WorldRender worldRender;
    private HudRender hudRender;
    private Tick tickHook;
    private Toggle onEnable;
    private Toggle onDisable;

    public Module(String name, String key, boolean enabled) {
        this.name = name;
        this.key = key;
        this.enabled = enabled;
    }

    // --- строители ---
    public Module add(Setting s) { settings.add(s); return this; }
    public Module world(WorldRender r) { this.worldRender = r; return this; }
    public Module hud(HudRender r) { this.hudRender = r; return this; }
    public Module tick(Tick t) { this.tickHook = t; return this; }
    public Module onEnable(Toggle t) { this.onEnable = t; return this; }
    public Module onDisable(Toggle t) { this.onDisable = t; return this; }

    // --- диспетчеризация ---
    public boolean hasSettings() { return !settings.isEmpty(); }
    public void doWorld(WorldRenderContext ctx) { if (worldRender != null) worldRender.render(ctx, this); }
    public void doHud(GuiGraphics g, float partial) { if (hudRender != null) hudRender.render(g, partial, this); }
    public void doTick(Minecraft mc) { if (tickHook != null) tickHook.tick(mc, this); }

    public void setEnabled(boolean b) {
        if (b == enabled) return;
        enabled = b;
        if (b) { if (onEnable != null) onEnable.run(this); }
        else { if (onDisable != null) onDisable.run(this); }
    }

    public void toggle() { setEnabled(!enabled); }

    // --- доступ к значениям настроек ---
    public Setting find(String n) {
        for (Setting s : settings) if (s.name.equals(n)) return s;
        return null;
    }

    public boolean bool(String n) {
        return find(n) instanceof Setting.Bool b && b.value;
    }

    public double num(String n) {
        return find(n) instanceof Setting.Num x ? x.value : 0.0;
    }

    public float numf(String n) {
        return (float) num(n);
    }

    public int inti(String n) {
        return (int) Math.round(num(n));
    }

    public String mode(String n) {
        return find(n) instanceof Setting.Mode m ? m.value() : "";
    }

    /** Итоговый ARGB настройки цвета (с учётом темы/радуги). */
    public int color(String n) {
        return find(n) instanceof Setting.Color c ? Colors.resolve(c) : 0xFFFFFFFF;
    }
}
