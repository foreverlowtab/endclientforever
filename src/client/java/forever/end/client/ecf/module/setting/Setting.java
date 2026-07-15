package forever.end.client.ecf.module.setting;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Настройка модуля. Подтипы: Bool (чекбокс), Num (слайдер), Mode (список), Color (цвет). */
public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    /** Логический переключатель. */
    public static final class Bool extends Setting {
        public boolean value;

        public Bool(String name, boolean value) {
            super(name);
            this.value = value;
        }
    }

    /** Число со слайдером (min..max, шаг). */
    public static final class Num extends Setting {
        public double value;
        public final double min;
        public final double max;
        public final double step;
        public final String suffix;

        public Num(String name, double value, double min, double max, double step) {
            this(name, value, min, max, step, "");
        }

        public Num(String name, double value, double min, double max, double step, String suffix) {
            super(name);
            this.value = value;
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix;
        }

        public double norm() {
            return (max <= min) ? 0.0 : (value - min) / (max - min);
        }

        public void setFromNorm(double n) {
            n = Math.max(0.0, Math.min(1.0, n));
            double raw = min + n * (max - min);
            if (step > 0) raw = Math.round(raw / step) * step;
            value = Math.max(min, Math.min(max, raw));
        }

        public int asInt() {
            return (int) Math.round(value);
        }

        public float asFloat() {
            return (float) value;
        }

        public String display() {
            if (step >= 1) return asInt() + suffix;
            return String.format(Locale.US, "%.2f", value) + suffix;
        }
    }

    /** Переключатель режимов (список строк). */
    public static final class Mode extends Setting {
        public int index;
        public final List<String> options;

        public Mode(String name, int index, String... opts) {
            super(name);
            this.options = Arrays.asList(opts);
            this.index = Math.max(0, Math.min(index, opts.length - 1));
        }

        public String value() {
            return options.get(index);
        }

        public void cycle(int dir) {
            int n = options.size();
            index = ((index + dir) % n + n) % n;
        }
    }

    /** Цвет (ARGB). syncTheme = акцент активной темы; rainbow = радуга. */
    public static final class Color extends Setting {
        public int argb;
        public boolean syncTheme;
        public boolean rainbow;

        public Color(String name, int argb, boolean syncTheme) {
            super(name);
            this.argb = argb;
            this.syncTheme = syncTheme;
        }
    }
}
