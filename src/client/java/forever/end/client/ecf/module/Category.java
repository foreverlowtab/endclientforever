package forever.end.client.ecf.module;

import java.util.List;

/** Категория модулей ClickGUI. */
public class Category {
    public final String name;
    public final String icon;
    public final List<Module> modules;

    public Category(String name, String icon, List<Module> modules) {
        this.name = name;
        this.icon = icon;
        this.modules = modules;
    }
}
