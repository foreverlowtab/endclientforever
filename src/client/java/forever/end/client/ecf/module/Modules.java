package forever.end.client.ecf.module;

import java.util.List;

/** Реестр визуальных модулей (зеркало CATEGORIES из HTML-прототипа). */
public final class Modules {
    private Modules() {}

    public static final List<Category> CATEGORIES = List.of(
        new Category("Cosmetics", "C", List.of(
            new Module("China Hat", "", true),
            new Module("Halo", "", false),
            new Module("Wings", "", true),
            new Module("Dragon Wings", "", false),
            new Module("Bunny Ears", "", false),
            new Module("Backpack", "", false),
            new Module("Pet", "", false))),
        new Category("Effects", "E", List.of(
            new Module("Jump Circles", "", true),
            new Module("Hit Effect", "", true),
            new Module("Kill Effect", "", false),
            new Module("Motion Trail", "", true),
            new Module("Damage Particles", "", false),
            new Module("Footprints", "", false),
            new Module("Block Overlay", "", false))),
        new Category("Animations", "A", List.of(
            new Module("Old Animations", "", true),
            new Module("3D Skin Layers", "", true),
            new Module("Emotes", "B", false),
            new Module("Custom Rotations", "", false),
            new Module("Sprint FX", "", true),
            new Module("Item Physics", "", false))),
        new Category("Camera", "K", List.of(
            new Module("Zoom", "C", true),
            new Module("Freelook", "V", false),
            new Module("No Hurt Cam", "", true),
            new Module("Cinematic Camera", "", false),
            new Module("FOV Changer", "", true),
            new Module("Motion Blur", "", false))),
        new Category("World", "W", List.of(
            new Module("Fullbright", "", true),
            new Module("Time Changer", "", false),
            new Module("Weather Changer", "", false),
            new Module("Custom Sky", "", false),
            new Module("Custom Clouds", "", false),
            new Module("Bloom", "", true))),
        new Category("HUD", "H", List.of(
            new Module("Watermark", "", true),
            new Module("FPS", "", true),
            new Module("Keystrokes", "", true),
            new Module("CPS", "", true),
            new Module("Coordinates", "", false),
            new Module("Armor HUD", "", true),
            new Module("Potion HUD", "", false),
            new Module("Clock", "", false))),
        new Category("Interface", "I", List.of(
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
}
