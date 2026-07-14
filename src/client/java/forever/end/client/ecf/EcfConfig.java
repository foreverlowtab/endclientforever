package forever.end.client.ecf;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Сохранение/загрузка настроек клиента (пока только тема). */
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
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("theme", ClientState.theme.name());
            try (OutputStream out = Files.newOutputStream(file())) {
                p.store(out, "End Client Forever");
            }
        } catch (Exception ignored) {
        }
    }
}
