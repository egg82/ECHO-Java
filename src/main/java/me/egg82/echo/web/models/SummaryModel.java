package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SummaryModel implements Serializable {
    private String id = "";
    private String output = "";

    public SummaryModel() { }

    public @NotNull String getId() { return id; }

    public void setId(@NotNull String id) { this.id = id; }

    public @NotNull String getOutput() { return output; }

    public void setOutput(@NotNull String output) { this.output = output; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SummaryModel)) return false;
        SummaryModel that = (SummaryModel) o;
        return id.equals(that.id) && output.equals(that.output);
    }

    public int hashCode() { return Objects.hash(id, output); }

    public String toString() {
        return "SummaryModel{" +
                "id='" + id + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}
