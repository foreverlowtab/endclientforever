package forever.end.client.ecf;

import java.util.ArrayList;
import java.util.List;

import forever.end.client.ecf.module.Category;
import forever.end.client.ecf.module.Module;
import forever.end.client.ecf.module.Modules;
import forever.end.client.ecf.module.setting.Setting;
import forever.end.client.ecf.net.EndApi;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Чат-команды клиента. Сообщения, начинающиеся с префикса (по умолчанию "."),
 * перехватываются и не уходят на сервер.
 *
 * Примеры:
 *   .cfg load “Мой конфиг”
 *   .toggle Fullbright
 *   .set "Custom Sky" "Плотность" 0.8
 */
public final class CommandManager {
    private CommandManager() {}

    public static String prefix = ".";
    private static boolean inited = false;

    public static void init() {
        if (inited) return;
        inited = true;
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            String m = message == null ? "" : message;
            if (m.startsWith(prefix)) {
                try { handle(m.substring(prefix.length())); }
                catch (Exception e) { reply("Ошибка команды: " + e.getMessage()); }
                return false; // не отправлять в чат сервера
            }
            return true;
        });
    }

    // ================= Разбор =================
    private static void handle(String raw) {
        List<String> t = tokenize(raw);
        if (t.isEmpty()) { help(); return; }
        String cmd = t.get(0).toLowerCase();
        List<String> a = t.subList(1, t.size());
        switch (cmd) {
            case "help", "?", "commands" -> help();
            case "cfg", "config" -> cfg(a);
            case "toggle", "t" -> toggle(a);
            case "enable", "on" -> setMod(a, true);
            case "disable", "off" -> setMod(a, false);
            case "set" -> setValue(a);
            case "settings", "info" -> settings(a);
            case "list", "modules" -> list(a);
            case "theme" -> theme(a);
            case "panic", "off!" -> panic();
            case "prefix" -> setPrefix(a);
            default -> reply("Неизвестная команда: " + cmd + ". Напиши " + prefix + "help");
        }
    }

    /** Разбивает строку на аргументы, учитывая кавычки (" “ ” « »). */
    private static List<String> tokenize(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean isQuote = c == '"' || c == '\u201C' || c == '\u201D' || c == '\u00AB' || c == '\u00BB';
            if (isQuote) { inQuote = !inQuote; continue; }
            if (Character.isWhitespace(c) && !inQuote) {
                if (cur.length() > 0) { out.add(cur.toString()); cur.setLength(0); }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    // ================= Команды =================
    private static void cfg(List<String> a) {
        if (a.isEmpty()) { reply("Использование: " + prefix + "cfg save|load|list|delete|cloud <имя>"); return; }
        String sub = a.get(0).toLowerCase();
        String name = a.size() > 1 ? String.join(" ", a.subList(1, a.size())) : "";
        switch (sub) {
            case "save" -> {
                if (name.isEmpty()) { reply("Укажи имя: " + prefix + "cfg save <имя>"); return; }
                reply(ConfigManager.saveNamed(name) ? ("Сохранён конфиг: " + name) : "Не удалось сохранить.");
            }
            case "load" -> {
                if (name.isEmpty()) { reply("Укажи имя: " + prefix + "cfg load <имя>"); return; }
                reply(ConfigManager.loadNamed(name) ? ("Конфиг загружен: " + name) : ("Конфиг не найден: " + name));
            }
            case "list" -> {
                List<String> names = ConfigManager.listNamed();
                reply(names.isEmpty() ? "Сохранённых конфигов нет." : ("Конфиги: " + String.join(", ", names)));
            }
            case "delete", "del", "remove" -> {
                if (name.isEmpty()) { reply("Укажи имя."); return; }
                reply(ConfigManager.deleteNamed(name) ? ("Удалён: " + name) : ("Не найден: " + name));
            }
            case "cloud" -> cfgCloud(a.size() > 1 ? a.get(1).toLowerCase() : "");
            default -> reply("Подкоманды: save, load, list, delete, cloud");
        }
    }

    private static void cfgCloud(String dir) {
        if (!ClientState.authed || ClientState.token.isEmpty()) { reply("Нужен вход в аккаунт."); return; }
        if (dir.equals("save")) {
            ConfigManager.saveLocal();
            reply("Отправка в облако…");
            EndApi.configSave(ClientState.token, ConfigManager.captureString(), r ->
                    reply(r.ok ? "Конфиг сохранён в облако." : ("Ошибка: " + r.message)));
        } else if (dir.equals("load")) {
            reply("Загрузка из облака…");
            EndApi.configLoad(ClientState.token, r -> {
                if (r.ok && r.data != null && r.data.has("data") && !r.data.get("data").isJsonNull()) {
                    String d = r.data.get("data").getAsString();
                    if (d.isEmpty()) { reply("В облаке пусто."); return; }
                    ConfigManager.applyString(d);
                    reply("Конфиг загружен из облака.");
                } else {
                    reply(r.ok ? "В облаке пусто." : ("Ошибка: " + r.message));
                }
            });
        } else {
            reply("Использование: " + prefix + "cfg cloud save|load");
        }
    }

    private static void toggle(List<String> a) {
        if (a.isEmpty()) { reply("Использование: " + prefix + "toggle <модуль>"); return; }
        Module m = findModule(String.join(" ", a));
        if (m == null) { reply("Модуль не найден."); return; }
        m.toggle();
        ConfigManager.saveLocal();
        reply(m.name + ": " + (m.enabled ? "включён" : "выключен"));
    }

    private static void setMod(List<String> a, boolean on) {
        if (a.isEmpty()) { reply("Использование: " + prefix + (on ? "enable" : "disable") + " <модуль>"); return; }
        Module m = findModule(String.join(" ", a));
        if (m == null) { reply("Модуль не найден."); return; }
        m.setEnabled(on);
        ConfigManager.saveLocal();
        reply(m.name + ": " + (on ? "включён" : "выключен"));
    }

    private static void setValue(List<String> a) {
        if (a.size() < 3) { reply("Использование: " + prefix + "set <модуль> <настройка> <значение>"); return; }
        Module m = findModule(a.get(0));
        if (m == null) { reply("Модуль не найден: " + a.get(0)); return; }
        Setting s = findSetting(m, a.get(1));
        if (s == null) { reply("Настройка не найдена: " + a.get(1)); return; }
        String val = String.join(" ", a.subList(2, a.size()));
        if (s instanceof Setting.Bool b) {
            b.value = parseBool(val, b.value);
        } else if (s instanceof Setting.Num n) {
            try { n.value = Math.max(n.min, Math.min(n.max, Double.parseDouble(val.replace(',', '.')))); }
            catch (NumberFormatException e) { reply("Нужно число (" + n.min + "…" + n.max + ")."); return; }
        } else if (s instanceof Setting.Mode md) {
            int idx = -1;
            try { idx = Integer.parseInt(val); } catch (NumberFormatException ignored) {}
            if (idx < 0) {
                for (int i = 0; i < md.options.size(); i++) if (md.options.get(i).equalsIgnoreCase(val)) { idx = i; break; }
            }
            if (idx < 0 || idx >= md.options.size()) { reply("Варианты: " + String.join(", ", md.options)); return; }
            md.index = idx;
        } else if (s instanceof Setting.Color col) {
            Integer argb = parseColor(val);
            if (argb == null) { reply("Цвет в формате #RRGGBB или #AARRGGBB."); return; }
            col.argb = argb; col.syncTheme = false; col.rainbow = false;
        } else {
            reply("Эту настройку нельзя задать командой."); return;
        }
        ConfigManager.saveLocal();
        reply(m.name + " · " + s.name + " = " + settingValue(s));
    }

    private static void settings(List<String> a) {
        if (a.isEmpty()) { reply("Использование: " + prefix + "settings <модуль>"); return; }
        Module m = findModule(String.join(" ", a));
        if (m == null) { reply("Модуль не найден."); return; }
        reply(m.name + " (" + (m.enabled ? "вкл" : "выкл") + "):");
        if (m.settings.isEmpty()) { reply("  нет настроек"); return; }
        for (Setting s : m.settings) reply("  " + s.name + " = " + settingValue(s));
    }

    private static void list(List<String> a) {
        boolean onlyOn = !a.isEmpty() && (a.get(0).equalsIgnoreCase("on") || a.get(0).equalsIgnoreCase("enabled"));
        for (Category c : Modules.CATEGORIES) {
            StringBuilder sb = new StringBuilder();
            for (Module m : c.modules) {
                if (onlyOn && !m.enabled) continue;
                if (sb.length() > 0) sb.append(", ");
                sb.append(m.name).append(m.enabled ? " [✓]" : "");
            }
            if (sb.length() > 0) reply(c.name + ": " + sb);
        }
    }

    private static void theme(List<String> a) {
        if (a.isEmpty()) { reply("Текущая тема: " + ClientState.theme.label + ". " + prefix + "theme <red|claude>"); return; }
        String v = a.get(0).toLowerCase();
        Theme t;
        if (v.startsWith("r")) t = Theme.RED;
        else if (v.startsWith("c")) t = Theme.CLAUDE;
        else { reply("Темы: red, claude"); return; }
        ClientState.setTheme(t);
        ConfigManager.saveLocal();
        reply("Тема: " + t.label);
    }

    private static void panic() {
        ConfigManager.allOff();
        ConfigManager.saveLocal();
        reply("Все функции выключены.");
    }

    private static void setPrefix(List<String> a) {
        if (a.isEmpty() || a.get(0).isEmpty()) { reply("Текущий префикс: " + prefix); return; }
        String p = a.get(0);
        if (p.length() != 1) { reply("Префикс — один символ."); return; }
        prefix = p;
        reply("Префикс команд: " + prefix);
    }

    private static void help() {
        reply("Команды (префикс «" + prefix + "»):");
        reply(prefix + "cfg save|load|list|delete <имя> — конфиги в папке");
        reply(prefix + "cfg cloud save|load — облачный конфиг");
        reply(prefix + "toggle <модуль> · " + prefix + "enable/disable <модуль>");
        reply(prefix + "set <модуль> <настройка> <значение>");
        reply(prefix + "settings <модуль> — показать настройки");
        reply(prefix + "list [on] — список модулей");
        reply(prefix + "theme <red|claude> · " + prefix + "panic — выключить всё");
        reply(prefix + "prefix <символ> — сменить префикс");
    }

    // ================= Вспомогательное =================
    private static Module findModule(String q) {
        if (q == null || q.isEmpty()) return null;
        String s = q.toLowerCase();
        Module partial = null;
        for (Category c : Modules.CATEGORIES) {
            for (Module m : c.modules) {
                if (m.name.equalsIgnoreCase(q)) return m;
                if (partial == null && m.name.toLowerCase().contains(s)) partial = m;
            }
        }
        return partial;
    }

    private static Setting findSetting(Module m, String q) {
        String s = q.toLowerCase();
        Setting partial = null;
        for (Setting st : m.settings) {
            if (st.name.equalsIgnoreCase(q)) return st;
            if (partial == null && st.name.toLowerCase().contains(s)) partial = st;
        }
        return partial;
    }

    private static String settingValue(Setting s) {
        if (s instanceof Setting.Bool b) return b.value ? "вкл" : "выкл";
        if (s instanceof Setting.Num n) return n.display();
        if (s instanceof Setting.Mode md) return md.value();
        if (s instanceof Setting.Color c) return String.format("#%08X", c.argb);
        return "?";
    }

    private static boolean parseBool(String v, boolean cur) {
        v = v.toLowerCase();
        if (v.equals("toggle")) return !cur;
        return v.equals("true") || v.equals("on") || v.equals("1") || v.equals("вкл") || v.equals("да");
    }

    private static Integer parseColor(String v) {
        v = v.trim();
        if (v.startsWith("#")) v = v.substring(1);
        try {
            if (v.length() == 6) return 0xFF000000 | Integer.parseInt(v, 16);
            if (v.length() == 8) return (int) Long.parseLong(v, 16);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private static void reply(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;
        int accent = ClientState.theme.accent & 0xFFFFFF;
        Component c = Component.literal("[ECF] ")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent)).withBold(true))
                .append(Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE8E8EC))));
        mc.gui.getChat().addMessage(c);
    }
}
