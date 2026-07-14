package forever.end.client.ecf.module;

/** Визуальный модуль (пока без логики — только имя, клавиша, состояние). */
public class Module {
    public final String name;
    public final String key;
    public boolean enabled;

    public Module(String name, String key, boolean enabled) {
        this.name = name;
        this.key = key;
        this.enabled = enabled;
    }
}
