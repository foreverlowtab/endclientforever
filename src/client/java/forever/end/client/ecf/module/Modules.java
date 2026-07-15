package forever.end.client.ecf.module;

import java.util.List;

import forever.end.client.ecf.fx.CosmeticFx;
import forever.end.client.ecf.fx.EffectFx;
import forever.end.client.ecf.module.setting.Setting;

/** Реестр визуальных модулей. Cosmetics и Effects — с реальными функциями, настройками и стилями. */
public final class Modules {
    private Modules() {}

    private static Setting.Color color() { return new Setting.Color("Цвет", 0xFFFFFFFF, true); }
    private static Setting.Mode style() { return new Setting.Mode("Стиль", 0, "Классик", "Неон"); }

    public static final List<Category> CATEGORIES = List.of(
        new Category("Cosmetics", "❖", List.of(
            new Module("China Hat", "", true)
                .add(new Setting.Num("Размер", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Num("Высота", 0.15, -0.2, 0.6, 0.05))
                .add(color())
                .world(CosmeticFx::chinaHat),
            new Module("Halo", "", false)
                .add(new Setting.Num("Размер", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Num("Высота", 0.15, -0.2, 0.6, 0.05))
                .add(color())
                .world(CosmeticFx::halo),
            new Module("Wings", "", true)
                .add(new Setting.Num("Размер", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Num("Скорость", 1.0, 0.0, 3.0, 0.1))
                .add(new Setting.Num("Раскрытие", 0.5, 0.0, 1.0, 0.05))
                .add(new Setting.Bool("Дракон", false))
                .add(color())
                .world(CosmeticFx::wings),
            new Module("Dragon Wings", "", false)
                .add(new Setting.Num("Размер", 1.3, 0.5, 2.5, 0.1))
                .add(new Setting.Num("Скорость", 1.0, 0.0, 3.0, 0.1))
                .add(new Setting.Num("Раскрытие", 0.6, 0.0, 1.0, 0.05))
                .add(new Setting.Bool("Дракон", true))
                .add(color())
                .world(CosmeticFx::wings),
            new Module("Bunny Ears", "", false)
                .add(new Setting.Num("Длина", 1.0, 0.6, 1.6, 0.05))
                .add(color())
                .world(CosmeticFx::bunnyEars),
            new Module("Backpack", "", false)
                .add(new Setting.Num("Размер", 1.0, 0.6, 1.8, 0.1))
                .add(color())
                .world(CosmeticFx::backpack),
            new Module("Pet", "", false)
                .add(new Setting.Num("Размер", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Num("Скорость", 1.0, 0.2, 3.0, 0.1))
                .add(new Setting.Mode("Тип", 0, "Куб", "Звезда"))
                .add(color())
                .world(CosmeticFx::pet))),
        new Category("Effects", "✦", List.of(
            new Module("Jump Circles", "", true)
                .add(style())
                .add(new Setting.Num("Размер", 1.2, 0.5, 3.0, 0.1))
                .add(new Setting.Num("Длительность", 14, 6, 40, 1))
                .add(color())
                .tick(EffectFx::jumpTick)
                .world(EffectFx::jumpRender),
            new Module("Hit Effect", "", true)
                .add(style())
                .add(new Setting.Mode("Режим", 0, "Искры", "Крит", "Сердца"))
                .add(new Setting.Num("Количество", 8, 3, 20, 1))
                .add(new Setting.Bool("Вспышка экрана", true))
                .add(color())
                .hud(EffectFx::hitHud),
            new Module("Kill Effect", "", false)
                .add(style())
                .add(new Setting.Mode("Режим", 0, "Взрыв", "Фейерверк", "Молния"))
                .add(color())
                .tick(EffectFx::killTick),
            new Module("Motion Trail", "", true)
                .add(style())
                .add(new Setting.Num("Длина", 18, 4, 40, 1))
                .add(new Setting.Num("Ширина", 1.0, 0.3, 2.5, 0.1))
                .add(color())
                .tick(EffectFx::trailTick)
                .world(EffectFx::trailRender),
            new Module("Damage Particles", "", false)
                .add(style())
                .add(new Setting.Num("Количество", 10, 4, 30, 1))
                .add(new Setting.Bool("Вспышка экрана", true))
                .add(color())
                .tick(EffectFx::dmgTick)
                .hud(EffectFx::dmgHud),
            new Module("Footprints", "", false)
                .add(style())
                .add(new Setting.Num("Размер", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Num("Длительность", 40, 10, 120, 1))
                .add(color())
                .tick(EffectFx::footTick)
                .world(EffectFx::footRender),
            new Module("Block Overlay", "", false)
                .add(style())
                .add(new Setting.Num("Прозрачность", 40, 6, 80, 1))
                .add(new Setting.Bool("Только контур", false))
                .add(color())
                .world(EffectFx::blockRender))),
        new Category("Animations", "◐", List.of(
            new Module("Old Animations", "", true),
            new Module("3D Skin Layers", "", true),
            new Module("Emotes", "B", false),
            new Module("Custom Rotations", "", false),
            new Module("Sprint FX", "", true),
            new Module("Item Physics", "", false))),
        new Category("Camera", "◉", List.of(
            new Module("Zoom", "C", true),
            new Module("Freelook", "V", false),
            new Module("No Hurt Cam", "", true),
            new Module("Cinematic Camera", "", false),
            new Module("FOV Changer", "", true),
            new Module("Motion Blur", "", false))),
        new Category("World", "☀", List.of(
            new Module("Fullbright", "", true),
            new Module("Time Changer", "", false),
            new Module("Weather Changer", "", false),
            new Module("Custom Sky", "", false),
            new Module("Custom Clouds", "", false),
            new Module("Bloom", "", true))),
        new Category("HUD", "▦", List.of(
            new Module("Watermark", "", true),
            new Module("FPS", "", true),
            new Module("Keystrokes", "", true),
            new Module("CPS", "", true),
            new Module("Coordinates", "", false),
            new Module("Armor HUD", "", true),
            new Module("Potion HUD", "", false),
            new Module("Clock", "", false))),
        new Category("Interface", "◈", List.of(
            new Module("Chroma", "", true),
            new Module("Custom Crosshair", "", true),
            new Module("Glint Colorizer", "", false),
            new Module("Rainbow Armor", "", false),
            new Module("ClickGUI", "R-Shift", true),
            new Module("Nametags", "", true),
            new Module("Menu Blur", "", false)))
    );

    public static int enabledCount() {
        int n = 0;
        for (Category c : CATEGORIES) {
            for (Module m : c.modules) {
                if (m.enabled) n++;
            }
        }
        return n;
    }

    /** Найти модуль по имени (null если нет). */
    public static Module find(String name) {
        for (Category c : CATEGORIES) {
            for (Module m : c.modules) {
                if (m.name.equals(name)) return m;
            }
        }
        return null;
    }
}
