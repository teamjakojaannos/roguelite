package fi.jakojaannos.roguelite.launcher.arguments;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class Arguments {
    private boolean ignoreUnknown = false;
    private List<Argument> arguments = new ArrayList<>();


    public static Arguments builder() {
        return new Arguments();
    }


    public Arguments ignoreUnknown() {
        this.ignoreUnknown = true;
        return this;
    }


    public Arguments with( Argument argument) {
        this.arguments.add(argument);
        return this;
    }

    public void consume( String... args) throws ArgumentParsingException {
        for (int i = 0; i < args.length; ++i) {
            val argStr = args[i];
            if (argStr.isEmpty()) {
                throw new ArgumentParsingException("Argument cannot be empty!");
            } else if (argStr.equals("-") || argStr.equals("--")) {
                throw new ArgumentParsingException("Hyphen(s) should be followed by argument name.");
            }

            String argName;
            if (argStr.startsWith("--")) {
                argName = argStr.substring(2);
            } else if (argStr.startsWith("-")) {
                argName = argStr.substring(1);
            } else {
                if (this.ignoreUnknown) {
                    continue;
                }

                throw new ArgumentParsingException(String.format(
                        "Got parameter \"%s\", when expecting an argument",
                        argStr
                ));
            }

            val params = new ArgumentParameters(i + 1, args);
            val argument = this.arguments.stream()
                                         .filter(a -> a.nameMatches(argName))
                                         .findFirst();

            if (argument.isPresent()) {
                argument.get().consumeArguments(params);
                i += params.getConsumed();
            } else if (!this.ignoreUnknown) {
                throw new UnknownArgumentException(String.format(
                        "Could not find argument with name or alias \"%s\"",
                        argName
                ));
            }
        }
    }
}
