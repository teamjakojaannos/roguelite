package fi.jakojaannos.roguelite.launcher.arguments;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Argument {
    @NonNull private final List<String> aliases;
    @NonNull private final Action action;

    @NonNull
    public static Builder withName(@NonNull String name, @NonNull String... aliases) {
        val aliasList = new ArrayList<String>(aliases.length + 1);
        aliasList.add(name);
        aliasList.addAll(Arrays.asList(aliases));
        return new Builder(aliasList);
    }

    boolean nameMatches(@NonNull String name) {
        return this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(name));
    }

    void consumeArguments(@NonNull ArgumentParameters params) throws ArgumentParsingException {
        this.action.perform(params);
    }

    @RequiredArgsConstructor
    public static class Builder {
        @NonNull private final List<String> aliases;

        @NonNull
        public Argument withAction(Action action) {
            return new Argument(this.aliases, action);
        }
    }

    public interface Action {
        void perform(@NonNull ArgumentParameters params) throws ArgumentParsingException;
    }
}
