package forever.end.client.ecf;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import forever.end.client.ecf.hud.HudElement;
import forever.end.client.ecf.hud.HudManager;
import net.fabricmc.loader.api.FabricLoader;

/** Сохранение/загрузка настроек клиента (тема + токен сессии + HUD). */
public final class EcfConfig {
    private EcfConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("endclientforever.properties");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(f)) {
                p.load(in);
            }
            String t = p.getProperty("theme");
            if (t != null) {
                try {
                    ClientState.theme = Theme.valueOf(t);
                } catch (IllegalArgumentException ignored) {
                }
            }
            String tok = p.getProperty("token");
            if (tok != null) ClientState.token = tok;
            String user = p.getProperty("username");
            if (user != null) ClientState.username = user;
        } catch (Exception ignored) {
        }
    }

    /** Загрузить позиции/масштаб HUD-элементов из конфига. */
    public static void loadHud(List<HudElement> els) {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(f)) {
                p.load(in);
            }
            for (HudElement e : els) {
                String bx = p.getProperty("hud." + e.id + ".x");
                String by = p.getProperty("hud." + e.id + ".y");
                String bs = p.getProperty("hud." + e.id + ".scale");
                if (bx != null) try { e.fx = Float.parseFloat(bx); } catch (NumberFormatException ignored) {}
                if (by != null) try { e.fy = Float.parseFloat(by); } catch (NumberFormatException ignored) {}
                if (bs != null) try { e.setScale(Float.parseFloat(bs)); } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("theme", ClientState.theme.name());
            p.setProperty("token", ClientState.token == null ? "" : ClientState.token);
            p.setProperty("username", ClientState.username == null ? "" : ClientState.username);
            for (HudElement e : HudManager.elements()) {
                p.setProperty("hud." + e.id + ".x", Float.toString(e.fx));
                p.setProperty("hud." + e.id + ".y", Float.toString(e.fy));
                p.setProperty("hud." + e.id + ".scale", Float.toString(e.scale));
            }
            try (OutputStream out = Files.newOutputStream(file())) {
                p.store(out, "End Client Forever");
            }
        } catch (Exception ignored) {
        }
    }
}
