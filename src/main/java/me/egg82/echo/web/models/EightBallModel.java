package me.egg82.echo.web.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class EightBallModel implements Serializable {
    private EightBallMagicModel magic = new EightBallMagicModel();

    public EightBallModel() { }

    public @NotNull EightBallMagicModel getMagic() { return magic; }

    public void setMagic(@NotNull EightBallMagicModel magic) { this.magic = magic; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EightBallModel)) return false;
        EightBallModel that = (EightBallModel) o;
        return magic.equals(that.magic);
    }

    @Override
    public int hashCode() { return Objects.hash(magic); }

    @Override
    public String toString() {
        return "EightBallModel{" +
                "magic=" + magic +
                '}';
    }

    public static class EightBallMagicModel implements Serializable {
        private String question = "";
        private String answer = "";
        private String type = "";

        public EightBallMagicModel() { }

        public @NotNull String getQuestion() { return question; }

        public void setQuestion(@NotNull String question) { this.question = question; }

        public @NotNull String getAnswer() { return answer; }

        public void setAnswer(@NotNull String answer) { this.answer = answer; }

        public @NotNull String getType() { return type; }

        public void setType(@NotNull String type) { this.type = type; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EightBallMagicModel)) return false;
            EightBallMagicModel that = (EightBallMagicModel) o;
            return question.equals(that.question) && answer.equals(that.answer) && type.equals(that.type);
        }

        @Override
        public int hashCode() { return Objects.hash(question, answer, type); }

        @Override
        public String toString() {
            return "EightBallMagicModel{" +
                    "question='" + question + '\'' +
                    ", answer='" + answer + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
