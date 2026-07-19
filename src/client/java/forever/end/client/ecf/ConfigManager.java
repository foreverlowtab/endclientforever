package forever.end.client.ecf;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import forever.end.client.ecf.module.Category;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Система конфигов: хранит включённость модулей, их настройки, тему и HUD
 * локально (JSON рядом с настройками игры) и умеет отдавать/принимать это для облака.
 *
 * Правила:
 *  - Первый запуск (файла нет): все функции ВЫКлючены, конфиг создаётся.
 *  - Следующие запуски: состояние восстанавливается таким, каким было при выходе.
 */
public final class ConfigManager {
    private ConfigManager() {}

    private static boolean loading = false;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("endclientforever-config.json");
    }

    /** Вызывается один раз при запуске (после ModuleManager.init()). */
    public static void init() {
        if (Files.exists(file())) {
            loadLocal();
        } else {
            allOff();      // первый запуск — всё выключено
            saveLocal();
        }
    }

    /** Выключить все модули (состояние по умолчанию при первом запуске). */
    public static void allOff() {
        loading = true;
        try {
            for (Category c : Modules.CATEGORIES) {
                for (Module m : c.modules) m.setEnabled(false);
            }
        } finally {
            loading = false;
        }
    }

    // ============ Сериализация ============
    public static JsonObject capture() {
        JsonObject root = new JsonObject();
        root.addProperty("theme", ClientState.theme.name());

        JsonObject mods = new JsonObject();
        for (Category c : Modules.CATEGORIES) {
            for (Module m : c.modules) {
                JsonObject mo = new JsonObject();
                mo.addProperty("on", m.enabled);
                JsonObject st = new JsonObject();
                for (Setting s : m.settings) {
                    if (s instanceof Setting.Bool b) {
                        st.addProperty(s.name, b.value);
                    } else if (s instanceof Setting.Num n) {
                        st.addProperty(s.name, n.value);
                    } else if (s instanceof Setting.Mode md) {
                        st.addProperty(s.name, md.index);
                    } else if (s instanceof Setting.Color col) {
                        JsonObject co = new JsonObject();
                        co.addProperty("argb", col.argb);
                        co.addProperty("syncTheme", col.syncTheme);
                        co.addProperty("rainbow", col.rainbow);
                        st.add(s.name, co);
                    }
                }
                mo.add("settings", st);
                mods.add(m.name, mo);
            }
        }
        root.add("modules", mods);

        JsonObject hud = new JsonObject();
        for (HudElement e : HudManager.elements()) {
            JsonObject he = new JsonObject();
            he.addProperty("offX", e.offX);
            he.addProperty("offY", e.offY);
            he.addProperty("scale", e.scale);
            hud.add(e.id, he);
        }
        root.add("hud", hud);
        return root;
    }

    public static void apply(JsonObject root) {
        if (root == null) return;
        loading = true;
        try {
            if (root.has("theme") && root.get("theme").isJsonPrimitive()) {
                try { ClientState.theme = Theme.valueOf(root.get("theme").getAsString()); }
                catch (IllegalArgumentException ignored) {}
            }
            if (root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject mods = root.getAsJsonObject("modules");
                for (Category c : Modules.CATEGORIES) {
                    for (Module m : c.modules) {
                        if (!mods.has(m.name) || !mods.get(m.name).isJsonObject()) continue;
                        JsonObject mo = mods.getAsJsonObject(m.name);
                        JsonObject st = mo.has("settings") && mo.get("settings").isJsonObject()
                                ? mo.getAsJsonObject("settings") : new JsonObject();
                        for (Setting s : m.settings) {
                            if (!st.has(s.name)) continue;
                            JsonElement v = st.get(s.name);
                            try {
                                if (s instanceof Setting.Bool b && v.isJsonPrimitive()) {
                                    b.value = v.getAsBoolean();
                                } else if (s instanceof Setting.Num n && v.isJsonPrimitive()) {
                                    n.value = Math.max(n.min, Math.min(n.max, v.getAsDouble()));
                                } else if (s instanceof Setting.Mode md && v.isJsonPrimitive()) {
                                    int idx = v.getAsInt();
                                    if (idx >= 0 && idx < md.options.size()) md.index = idx;
                                } else if (s instanceof Setting.Color col && v.isJsonObject()) {
                                    JsonObject co = v.getAsJsonObject();
                                    if (co.has("argb")) col.argb = co.get("argb").getAsInt();
                                    if (co.has("syncTheme")) col.syncTheme = co.get("syncTheme").getAsBoolean();
                                    if (co.has("rainbow")) col.rainbow = co.get("rainbow").getAsBoolean();
                                }
                            } catch (Exception ignored) {}
                        }
                        boolean on = mo.has("on") && mo.get("on").getAsBoolean();
                        m.setEnabled(on);
                    }
                }
            }
            if (root.has("hud") && root.get("hud").isJsonObject()) {
                JsonObject hud = root.getAsJsonObject("hud");
                for (HudElement e : HudManager.elements()) {
                    if (!hud.has(e.id) || !hud.get(e.id).isJsonObject()) continue;
                    JsonObject he = hud.getAsJsonObject(e.id);
                    try {
                        if (he.has("offX")) e.offX = he.get("offX").getAsInt();
                        if (he.has("offY")) e.offY = he.get("offY").getAsInt();
                        if (he.has("scale")) e.setScale(he.get("scale").getAsFloat());
                    } catch (Exception ignored) {}
                }
            }
        } finally {
            loading = false;
        }
    }

    // ============ Локально ============
    public static void saveLocal() {
        if (loading) return;
        try {
            Files.writeString(file(), capture().toString());
        } catch (Exception ignored) {}
    }

    public static void loadLocal() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            JsonElement el = JsonParser.parseString(Files.readString(f));
            if (el.isJsonObject()) apply(el.getAsJsonObject());
        } catch (Exception ignored) {}
    }

    // ============ Облако ============
    public static String captureString() {
        return capture().toString();
    }

    public static void applyString(String json) {
        try {
            JsonElement el = JsonParser.parseString(json);
            if (el.isJsonObject()) { apply(el.getAsJsonObject()); saveLocal(); }
        } catch (Exception ignored) {}
    }

    // ============ Именованные конфиги (папка config/endclientforever/configs) ============
    public static Path configsDir() {
        Path d = FabricLoader.getInstance().getConfigDir().resolve("endclientforever").resolve("configs");
        try { Files.createDirectories(d); } catch (Exception ignored) {}
        return d;
    }

    private static String sanitize(String name) {
        StringBuilder sb = new StringBuilder();
        for (char ch : name.trim().toCharArray()) {
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == ' ') sb.append(ch);
            else sb.append('_');
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? "config" : s;
    }

    /** Сохранить текущее состояние как именованный конфиг в папке. */
    public static boolean saveNamed(String name) {
        try {
            Files.writeString(configsDir().resolve(sanitize(name) + ".json"), capture().toString());
            return true;
        } catch (Exception e) { return false; }
    }

    /** Загрузить именованный конфиг из папки и применить его. */
    public static boolean loadNamed(String name) {
        try {
            Path f = configsDir().resolve(sanitize(name) + ".json");
            if (!Files.exists(f)) return false;
            JsonElement el = JsonParser.parseString(Files.readString(f));
            if (el.isJsonObject()) { apply(el.getAsJsonObject()); saveLocal(); return true; }
            return false;
        } catch (Exception e) { return false; }
    }

    public static boolean deleteNamed(String name) {
        try { return Files.deleteIfExists(configsDir().resolve(sanitize(name) + ".json")); }
        catch (Exception e) { return false; }
    }

    public static java.util.List<String> listNamed() {
        java.util.List<String> out = new java.util.ArrayList<>();
        try (java.util.stream.Stream<Path> st = Files.list(configsDir())) {
            st.filter(x -> x.getFileName().toString().endsWith(".json")).forEach(x -> {
                String n = x.getFileName().toString();
                out.add(n.substring(0, n.length() - 5));
            });
        } catch (Exception ignored) {}
        java.util.Collections.sort(out);
        return out;
    }
}
