package me.egg82.echo.web.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class SummaryModel implements Serializable {
    private String id = "";
    private String output = "";

    public SummaryModel() { }

    public @NotNull String getId() { return id; }

    public void setId(@NotNull String id) { this.id = id; }

    public @NotNull String getOutput() { return output; }

    public void setOutput(@NotNull String output) { this.output = output; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SummaryModel)) return false;
        SummaryModel that = (SummaryModel) o;
        return id.equals(that.id) && output.equals(that.output);
    }

    @Override
    public int hashCode() { return Objects.hash(id, output); }

    @Override
    public String toString() {
        return "SummaryModel{" +
                "id='" + id + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}
