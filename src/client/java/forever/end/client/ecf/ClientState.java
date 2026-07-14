package forever.end.client.ecf;

import com.google.gson.JsonObject;

import forever.end.client.ecf.net.EndApi;

/** Глобальное состояние клиента (авторизация, профиль, тема). */
public final class ClientState {
    private ClientState() {}

    // Сессия
    public static boolean authed = false;
    public static String token = "";

    // Профиль
    public static String username = "";
    public static String displayName = "";
    public static String role = "user";
    public static String roleLabel = "Участник";
    public static String avatarColor = "#e11d2a";
    public static int downloads = 0;
    public static String memberSince = "—";

    // Подписка
    public static boolean subActive = false;
    public static String subPlan = "Forever Lite";
    public static String subExpires = "none"; // "none" | "forever" | "dd.mm.yyyy"

    // Скачивание
    public static boolean canDownload = false;
    public static String downloadUrl = "https://endclient.fun";

    // Настройки
    public static Theme theme = Theme.RED;

    public static void setTheme(Theme t) {
        theme = t;
        EcfConfig.save();
        event("theme", "Тема: " + t.name());
    }

    /** Заполнить состояние из ответа API (login.php / me.php). */
    public static void applyLogin(String tok, JsonObject root) {
        token = tok == null ? "" : tok;
        authed = true;

        if (root != null && root.has("user") && root.get("user").isJsonObject()) {
            JsonObject u = root.getAsJsonObject("user");
            username = str(u, "username", username);
            displayName = str(u, "display_name", username);
            role = str(u, "role", "user");
            roleLabel = str(u, "role_label", roleLabel);
            avatarColor = str(u, "avatar_color", avatarColor);
            downloads = intv(u, "downloads", 0);
            memberSince = str(u, "member_since", "—");
        }
        if (root != null && root.has("subscription") && root.get("subscription").isJsonObject()) {
            JsonObject s = root.getAsJsonObject("subscription");
            subActive = s.has("active") && s.get("active").getAsBoolean();
            subPlan = str(s, "plan", subPlan);
            subExpires = str(s, "expires", "none");
        }
        if (root != null && root.has("download") && root.get("download").isJsonObject()) {
            JsonObject d = root.getAsJsonObject("download");
            canDownload = d.has("allowed") && d.get("allowed").getAsBoolean();
            downloadUrl = str(d, "url", downloadUrl);
        }
        EcfConfig.save();
    }

    public static void logout() {
        if (!token.isEmpty()) {
            EndApi.logout(token, r -> {});
        }
        authed = false;
        token = "";
        username = "";
        displayName = "";
        role = "user";
        roleLabel = "Участник";
        downloads = 0;
        subActive = false;
        subExpires = "none";
        canDownload = false;
        downloadUrl = "https://endclient.fun";
        EcfConfig.save();
    }

    /** Отправить действие в лог сайта (видно в админке). */
    public static void event(String type, String detail) {
        if (authed && !token.isEmpty()) {
            EndApi.event(token, type, detail);
        }
    }

    /** Красивое описание подписки для UI. */
    public static String subLabel() {
        if (!subActive) return "Подписка не активна";
        if ("forever".equals(subExpires)) return subPlan + " · навсегда";
        if ("none".equals(subExpires) || subExpires.isEmpty()) return subPlan + " · активна";
        return subPlan + " · до " + subExpires;
    }

    private static String str(JsonObject o, String k, String def) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : def;
    }

    private static int intv(JsonObject o, String k, int def) {
        try {
            return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : def;
        } catch (Exception e) {
            return def;
        }
    }
}
