package fi.jakojaannos.roguelite.launcher.arguments;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class Parameter<T> {
    @NonNull
    protected final String name;

    @NonNull
    public abstract T parse(String string) throws ArgumentParsingException;

    @NonNull
    public static <T> Parameter<Optional<T>> optional(Parameter<T> parameter) {
        return null;
    }

    @NonNull
    public static IntegerParameter integer(@NonNull String name) {
        return new IntegerParameter(name);
    }

    @NonNull
    public static <T extends Enum<T>> EnumParameter<T> enumeration(
            @NonNull String name,
            @NonNull Class<T> enumClass
    ) {
        return new EnumParameter<>(name, enumClass);
    }

    public static BoolParameter bool(@NonNull String name) {
        return new BoolParameter(name);
    }

    public static PathParameter filePath(@NonNull String name) {
        return new PathParameter(name);
    }

    public static class BoolParameter extends Parameter<Boolean> {
        private BoolParameter(@NonNull String name) {
            super(name);
        }

        @Override
        public Boolean parse(String string) {
            return Boolean.valueOf(string);
        }
    }

    public static class IntegerParameter extends Parameter<Integer> {
        private int minimum;
        private boolean hasMinimum = false;

        private IntegerParameter(@NonNull String name) {
            super(name);
        }

        @NonNull
        public IntegerParameter withMin(int minimum) {
            this.minimum = minimum;
            this.hasMinimum = true;
            return this;
        }

        @NonNull
        @Override
        public Integer parse(String string) throws ArgumentParsingException {
            try {
                val value = Integer.parseInt(string);
                if (this.hasMinimum && value < this.minimum) {
                    throw new ArgumentOutOfBoundsException(String.format(
                            "Given %s is out of bounds! Expected minimum of %d, got %d",
                            this.name,
                            this.minimum,
                            value
                    ));
                }
                return value;
            } catch (NumberFormatException e) {
                throw new ArgumentParsingException(String.format(
                        "Invalid integer \"%s\" passed as \"%s\"",
                        string,
                        this.name
                ));
            }
        }
    }

    public static class EnumParameter<T extends Enum<T>> extends Parameter<T> {
        private final Class<T> type;

        private EnumParameter(@NonNull String name, @NonNull Class<T> type) {
            super(name);
            this.type = type;
        }

        @NonNull
        @Override
        public T parse(@NonNull String string) throws ArgumentParsingException {
            try {
                return T.valueOf(this.type, string);
            } catch (IllegalArgumentException e) {
                throw new ArgumentParsingException("String \"" + string + "\" does not represent valid constant of the enum \"" + this.type.getSimpleName() + "\"", e);
            }
        }
    }

    public static class PathParameter extends Parameter<String> {
        private boolean mustBeDirectory;
        private boolean mustExist;

        private PathParameter(@NonNull String name) {
            super(name);
        }

        public PathParameter mustBeDirectory() {
            this.mustExist = true;
            this.mustBeDirectory = true;
            return this;
        }

        public PathParameter mustExist() {
            this.mustExist = true;
            return this;
        }

        @Override
        public String parse(String string) throws ArgumentParsingException {
            val file = Paths.get(string).toFile();
            if (this.mustExist && !file.exists()) {
                throw new ArgumentParsingException(String.format(
                        "File/directory in path \"%s\" does not exist",
                        string
                ));
            }

            if (this.mustBeDirectory && !file.isDirectory()) {
                throw new ArgumentParsingException(String.format(
                        "File/path \"%s\" is not a directory",
                        string
                ));
            }
            return string;
        }
    }
}
