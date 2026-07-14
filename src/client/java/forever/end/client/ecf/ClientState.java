package forever.end.client.ecf;

/** Глобальное состояние клиента (авторизация, тема, пользователь). */
public final class ClientState {
    private ClientState() {}

    public static boolean authed = false;
    public static String username = "";
    public static String role = "Владелец";
    public static Theme theme = Theme.RED;

    public static void setTheme(Theme t) {
        theme = t;
        EcfConfig.save();
    }

    public static void login(String name) {
        authed = true;
        username = name;
    }

    public static void logout() {
        authed = false;
    }
}
