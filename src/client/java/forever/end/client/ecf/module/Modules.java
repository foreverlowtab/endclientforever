package forever.end.client.ecf.module;

import java.util.List;

import forever.end.client.ecf.fx.AnimationFx;
import forever.end.client.ecf.fx.WorldFx;
import forever.end.client.ecf.fx.CameraFx;
import forever.end.client.ecf.fx.CosmeticFx;
import forever.end.client.ecf.fx.EffectFx;
import forever.end.client.ecf.fx.InterfaceFx;
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
            new Module("Old Animations", "", true)
                .add(new Setting.Mode("Стиль", 0, "Дуга", "Клинок", "Круг"))
                .add(new Setting.Num("Длина", 1.0, 0.5, 2.0, 0.1))
                .add(new Setting.Color("Цвет", 0xFFFFFFFF, true))
                .world(AnimationFx::oldSwingRender),
            new Module("3D Skin Layers", "", true)
                .add(new Setting.Mode("Слои", 1, "Голова", "Голова+тело", "Полностью"))
                .add(new Setting.Num("Толщина", 0.06, 0.02, 0.15, 0.01))
                .add(new Setting.Num("Прозрачность", 35, 10, 90, 1))
                .add(new Setting.Color("Цвет", 0xFFFFFFFF, true))
                .world(AnimationFx::skinLayers),
            new Module("Emotes", "B", false)
                .add(new Setting.Mode("Режим", 0, "Эффект", "Модель"))
                .add(new Setting.Mode("Эмоция", 0, "Привет", "Сердце", "Салют", "Спираль"))
                .add(new Setting.Mode("Анимация", 0, "Привет", "Руки вверх", "Аплодисменты", "Кивок", "Нет", "Танец 1", "Танец 2"))
                .add(new Setting.Num("Скорость", 1.0, 0.3, 2.5, 0.1))
                .add(new Setting.Bool("Подпись", true))
                .add(new Setting.Color("Цвет", 0xFFE11D2A, false))
                .tick(AnimationFx::emoteTick)
                .world(AnimationFx::emoteRender)
                .onDisable(AnimationFx::emoteReset),
            new Module("Custom Rotations", "", false)
                .add(new Setting.Mode("Тело", 0, "Статичное", "Вперёд по камере", "Ванильное"))
                .tick(AnimationFx::rotationTick)
                .onDisable(AnimationFx::rotationReset),
            new Module("Sprint FX", "", true)
                .add(new Setting.Mode("Стиль", 0, "Линии", "Туннель", "Частицы"))
                .add(new Setting.Num("Интенсивность", 55, 10, 100, 1))
                .add(new Setting.Color("Цвет", 0xFFFFFFFF, true))
                .tick(AnimationFx::sprintTick)
                .hud(AnimationFx::sprintHud),
            new Module("Item Physics", "", false)
                .add(new Setting.Mode("Стиль", 0, "Кольцо", "Свечение", "Подпись"))
                .add(new Setting.Num("Радиус", 6, 2, 16, 1))
                .add(new Setting.Color("Цвет", 0xFFFFFFFF, true))
                .world(AnimationFx::itemPhysics))),
        new Category("Camera", "◉", List.of(
            new Module("Zoom", "C", true)
                .add(new Setting.Num("Кратность", 3.0, 1.5, 5.0, 0.5))
                .add(new Setting.Bool("Плавно", true))
                .add(new Setting.Num("Скорость", 0.5, 0.1, 1.0, 0.05)),
            new Module("Freelook", "V", false),
            new Module("No Hurt Cam", "", true),
            new Module("Cinematic Camera", "", false),
            new Module("FOV Changer", "", false)
                .add(new Setting.Num("FOV", 90, 30, 110, 1)),
            new Module("Motion Blur", "", false)
                .add(new Setting.Num("Интенсивность", 45, 10, 90, 1))
                .hud(CameraFx::motionHud))),
        new Category("World", "☀", List.of(
            new Module("Fullbright", "", true)
                .add(new Setting.Num("Яркость", 1.0, 0.5, 1.0, 0.05))
                .tick(WorldFx::fullbrightTick)
                .onDisable(WorldFx::fullbrightReset),
            new Module("Time Changer", "", false)
                .add(new Setting.Mode("Время", 1, "Утро", "Полдень", "Закат", "Ночь", "Полночь"))
                .tick(WorldFx::timeTick),
            new Module("Weather Changer", "", false)
                .add(new Setting.Mode("Погода", 0, "Ясно", "Дождь", "Гроза"))
                .tick(WorldFx::weatherTick)
                .onDisable(WorldFx::weatherReset),
            new Module("Custom Sky", "", false)
                .add(new Setting.Color("Цвет", 0xFF7FB2FF, false))
                .add(new Setting.Num("Плотность", 0.3, 0.0, 0.7, 0.05))
                .hud(WorldFx::skyHud),
            new Module("Custom Clouds", "", false)
                .add(new Setting.Mode("Облака", 2, "Выкл", "Быстрые", "Детальные"))
                .tick(WorldFx::cloudsTick)
                .onDisable(WorldFx::cloudsReset),
            new Module("Bloom", "", true)
                .add(new Setting.Num("Интенсивность", 0.5, 0.0, 1.0, 0.05))
                .hud(WorldFx::bloomHud))),
        new Category("HUD", "▦", List.of(
            new Module("Watermark", "", true)
                .add(new Setting.Bool("Профиль", true))
                .add(new Setting.Bool("Версия", true)),
            new Module("FPS", "", true)
                .add(new Setting.Bool("График", true)),
            new Module("Keystrokes", "", true)
                .add(new Setting.Bool("Пробел", true))
                .add(new Setting.Bool("Кнопки мыши", true)),
            new Module("CPS", "", true)
                .add(new Setting.Bool("ЛКМ+ПКМ", true)),
            new Module("Coordinates", "", false)
                .add(new Setting.Bool("Направление", true))
                .add(new Setting.Bool("Биом", false)),
            new Module("Armor HUD", "", true)
                .add(new Setting.Bool("Прочность", true))
                .add(new Setting.Bool("Рука", true)),
            new Module("Potion HUD", "", false)
                .add(new Setting.Bool("Время", true)),
            new Module("Clock", "", false)
                .add(new Setting.Bool("Секунды", true))
                .add(new Setting.Mode("Формат", 0, "24ч", "12ч"))
                .add(new Setting.Bool("Дата", false)))),
        new Category("Interface", "◈", List.of(
            new Module("Chroma", "", true)
                .add(new Setting.Num("Скорость", 1.0, 0.2, 3.0, 0.1))
                .add(new Setting.Num("Насыщенность", 0.85, 0.3, 1.0, 0.05)),
            new Module("Custom Crosshair", "", true)
                .add(new Setting.Mode("Стиль", 0, "Крест", "Точка", "Круг", "Т-образный", "Крест+точка"))
                .add(new Setting.Num("Размер", 5, 1, 12, 1))
                .add(new Setting.Num("Зазор", 2, 0, 8, 1))
                .add(new Setting.Num("Толщина", 1, 1, 4, 1))
                .add(new Setting.Bool("Контур", true))
                .add(color())
                .hud(InterfaceFx::crosshairHud),
            new Module("Glint Colorizer", "", false)
                .add(new Setting.Mode("Режим", 0, "Радуга", "Акцент", "Свой"))
                .add(new Setting.Num("Скорость", 1.0, 0.2, 3.0, 0.1))
                .add(new Setting.Num("Интенсивность", 55, 10, 90, 1))
                .add(color())
                .hud(InterfaceFx::glintHud),
            new Module("Rainbow Armor", "", false)
                .add(new Setting.Mode("Режим", 0, "Радуга", "Акцент"))
                .add(new Setting.Num("Скорость", 1.0, 0.2, 3.0, 0.1))
                .add(new Setting.Num("Прозрачность", 45, 10, 90, 1))
                .world(InterfaceFx::rainbowArmor),
            new Module("ClickGUI", "R-Shift", true)
                .add(new Setting.Bool("R-Shift", true))
                .add(new Setting.Bool("Из меню", true)),
            new Module("Nametags", "", true)
                .add(new Setting.Bool("Здоровье", true))
                .add(new Setting.Num("Радиус", 48, 8, 128, 1))
                .add(new Setting.Num("Масштаб", 1.0, 0.5, 2.0, 0.05))
                .add(new Setting.Bool("Свои", false))
                .world(InterfaceFx::nametags),
            new Module("Menu Blur", "", false)
                .add(new Setting.Num("Интенсивность", 55, 20, 95, 1))
                .add(new Setting.Bool("В игре", true))))
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
