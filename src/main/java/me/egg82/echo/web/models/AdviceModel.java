package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class AdviceModel implements Serializable {
    private AdviceSlipModel slip = new AdviceSlipModel();

    public AdviceModel() { }

    public @NotNull AdviceSlipModel getSlip() { return slip; }

    public void setSlip(@NotNull AdviceSlipModel slip) { this.slip = slip; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdviceModel)) return false;
        AdviceModel that = (AdviceModel) o;
        return slip.equals(that.slip);
    }

    public int hashCode() { return Objects.hash(slip); }

    public String toString() {
        return "AdviceModel{" +
                "slip=" + slip +
                '}';
    }

    public static final class AdviceSlipModel implements Serializable {
        private long id = -1L;
        private String advice = "";

        public AdviceSlipModel() { }

        public long getId() { return id; }

        public void setId(long id) { this.id = id; }

        public @NotNull String getAdvice() { return advice; }

        public void setAdvice(@NotNull String advice) { this.advice = advice; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AdviceSlipModel)) return false;
            AdviceSlipModel that = (AdviceSlipModel) o;
            return id == that.id && advice.equals(that.advice);
        }

        public int hashCode() { return Objects.hash(id, advice); }

        public String toString() {
            return "AdviceSlipModel{" +
                    "id=" + id +
                    ", advice='" + advice + '\'' +
                    '}';
        }
    }
}
