package forever.end.client.ecf.net;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;

/**
 * Сетевой слой клиента: обращение к API сайта endclient.fun.
 * Все запросы асинхронные; callback вызывается в основном потоке Minecraft.
 */
public final class EndApi {
    private EndApi() {}

    public static final String BASE = "https://endclient.fun/api";
    private static final String UA = "EndClientForever/0.6-test (Fabric 1.21.4)";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /** Результат запроса. data может быть null при ошибке. */
    public static final class Result {
        public final boolean ok;
        public final String message;
        public final JsonObject data;

        Result(boolean ok, String message, JsonObject data) {
            this.ok = ok;
            this.message = message;
            this.data = data;
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static HttpRequest post(String path, String form) {
        return HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json")
                .header("User-Agent", UA)
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
    }

    private static Result parse(HttpResponse<String> resp) {
        try {
            JsonObject o = JsonParser.parseString(resp.body()).getAsJsonObject();
            boolean ok = o.has("ok") && o.get("ok").getAsBoolean();
            String msg = o.has("message") && !o.get("message").isJsonNull() ? o.get("message").getAsString() : "";
            return new Result(ok, msg, o);
        } catch (Exception e) {
            return new Result(false, "Некорректный ответ сервера (" + resp.statusCode() + ")", null);
        }
    }

    private static void send(HttpRequest req, Consumer<Result> cb) {
        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(EndApi::parse)
                .exceptionally(ex -> new Result(false, "Нет соединения с сервером", null))
                .thenAccept(r -> Minecraft.getInstance().execute(() -> cb.accept(r)));
    }

    /** Вход по логину/паролю. */
    public static void login(String user, String pass, Consumer<Result> cb) {
        send(post("/login.php", "username=" + enc(user) + "&password=" + enc(pass)), cb);
    }

    /** Проверка токена и получение свежего профиля. */
    public static void me(String token, Consumer<Result> cb) {
        send(post("/me.php", "token=" + enc(token)), cb);
    }

    /** Завершить сессию. */
    public static void logout(String token, Consumer<Result> cb) {
        send(post("/logout.php", "token=" + enc(token)), cb);
    }

    /** Отправить событие (fire-and-forget, ответ не важен). */
    public static void event(String token, String type, String detail) {
        if (token == null || token.isEmpty()) return;
        try {
            HttpRequest req = post("/event.php",
                    "token=" + enc(token) + "&type=" + enc(type) + "&detail=" + enc(detail));
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }
}
