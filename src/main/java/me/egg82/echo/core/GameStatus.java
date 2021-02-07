package me.egg82.echo.core;

import java.io.Serializable;
import java.util.Objects;
import me.egg82.echo.utils.TimeUtil;
import org.jetbrains.annotations.NotNull;

public class GameStatus implements Serializable {
    private final String name;
    private final String displayName;
    private final TimeUtil.Time min;
    private final TimeUtil.Time max;

    public GameStatus(@NotNull String name, @NotNull String displayName, @NotNull TimeUtil.Time min, @NotNull TimeUtil.Time max) {
        this.name = name;
        this.displayName = displayName;
        this.min = min;
        this.max = max;
    }

    public @NotNull String getName() { return name; }

    public @NotNull String getDisplayName() { return displayName; }

    public @NotNull TimeUtil.Time getMin() { return min; }

    public @NotNull TimeUtil.Time getMax() { return max; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameStatus)) return false;
        GameStatus that = (GameStatus) o;
        return name.equals(that.name) && displayName.equals(that.displayName) && min.equals(that.min) && max.equals(that.max);
    }

    public int hashCode() { return Objects.hash(name, displayName, min, max); }

    public String toString() {
        return "GameStatus{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
