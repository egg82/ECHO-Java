package me.egg82.echo.web.models;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XKCDSearchModel implements Serializable {
    private float weight = Float.NaN;
    private int selection = -1;
    private final List<IntObjectImmutablePair<String>> comics = new ArrayList<>();

    public XKCDSearchModel() { }

    public float getWeight() { return weight; }

    public void setWeight(float weight) { this.weight = weight; }

    public int getSelection() { return selection; }

    public void setSelection(int selection) { this.selection = selection; }

    public @NotNull List<IntObjectImmutablePair<String>> getComics() { return comics; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof XKCDSearchModel)) {
            return false;
        }
        XKCDSearchModel that = (XKCDSearchModel) o;
        return Float.compare(that.weight, weight) == 0 && selection == that.selection && comics.equals(that.comics);
    }

    @Override
    public int hashCode() { return Objects.hash(weight, selection, comics); }

    @Override
    public String toString() {
        return "XKCDSearchModel{" +
                "weight=" + weight +
                ", selection=" + selection +
                ", comics=" + comics +
                '}';
    }
}
