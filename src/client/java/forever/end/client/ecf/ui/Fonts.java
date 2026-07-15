package forever.end.client.ecf.ui;

import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * Кастомные шрифты клиента (веб-шрифты сайта, встроены в ресурс-пак).
 *  - serif   → Georgia (дисплейный шрифт темы Claude + большой тайтл)
 *  - grotesk → DejaVu Sans Bold (дисплейный шрифт темы Red, замена Space Grotesk)
 *  - body    → DejaVu Sans (основной текст, замена Inter; полное покрытие кириллицы и символов)
 * Иконки-глифы (▶ ◆ ⚙ ⏻ ✕ и т.п.) НЕ оборачиваем — они рисуются дефолтным шрифтом MC,
 * чтобы работал фолбэк Unifont и не было "тофу".
 */
public final class Fonts {
    private Fonts() {}

    public static final ResourceLocation SERIF =
            ResourceLocation.fromNamespaceAndPath("forever-endclient", "serif");
    public static final ResourceLocation GROTESK =
            ResourceLocation.fromNamespaceAndPath("forever-endclient", "grotesk");
    public static final ResourceLocation BODY =
            ResourceLocation.fromNamespaceAndPath("forever-endclient", "body");

    public static Component with(String s, ResourceLocation f) {
        return Component.literal(s).setStyle(Style.EMPTY.withFont(f));
    }

    public static Component body(String s) { return with(s, BODY); }
    public static Component serif(String s) { return with(s, SERIF); }
    public static Component grotesk(String s) { return with(s, GROTESK); }

    /** Дисплейный шрифт по активной теме: Claude → serif, Red → grotesk. */
    public static ResourceLocation displayFont() {
        return ClientState.theme == Theme.CLAUDE ? SERIF : GROTESK;
    }

    public static Component display(String s) {
        return with(s, displayFont());
    }
}
