package fi.jakojaannos.roguelite.launcher.arguments;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ArgumentParameters {
    private final int beginIndex;
    private final @NonNull String[] args;

    @Getter(AccessLevel.PACKAGE) private int consumed = 0;

    ArgumentParameters(int beginIndex, @NonNull String[] args) {
        this.beginIndex = beginIndex;
        this.args = args;
    }

    @NonNull
    public <T> T parameter(@NonNull Parameter<T> parameter) throws ArgumentParsingException {
        ++this.consumed;
        if (this.consumed >= this.args.length) {
            throw new ArgumentParsingException("Not enough parameters provided");
        }

        val str = this.args[this.beginIndex + (this.consumed - 1)];
        if (str.startsWith("-")) {
            throw new ArgumentParsingException(String.format(
                    "Not enough parameters provided. Got next argument (%s) as parameter string " +
                            "while parsing!",
                    str
            ));
        }
        return parameter.parse(str);
    }
}
