package me.egg82.echo.web.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class AdviceModel implements Serializable {
    private AdviceSlipModel slip = new AdviceSlipModel();

    public AdviceModel() { }

    public @NotNull AdviceSlipModel getSlip() { return slip; }

    public void setSlip(@NotNull AdviceSlipModel slip) { this.slip = slip; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdviceModel)) return false;
        AdviceModel that = (AdviceModel) o;
        return slip.equals(that.slip);
    }

    @Override
    public int hashCode() { return Objects.hash(slip); }

    @Override
    public String toString() {
        return "AdviceModel{" +
                "slip=" + slip +
                '}';
    }

    public static class AdviceSlipModel implements Serializable {
        private long id = -1L;
        private String advice = "";

        public AdviceSlipModel() { }

        public long getId() { return id; }

        public void setId(long id) { this.id = id; }

        public @NotNull String getAdvice() { return advice; }

        public void setAdvice(@NotNull String advice) { this.advice = advice; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AdviceSlipModel)) return false;
            AdviceSlipModel that = (AdviceSlipModel) o;
            return id == that.id && advice.equals(that.advice);
        }

        @Override
        public int hashCode() { return Objects.hash(id, advice); }

        @Override
        public String toString() {
            return "AdviceSlipModel{" +
                    "id=" + id +
                    ", advice='" + advice + '\'' +
                    '}';
        }
    }
}
