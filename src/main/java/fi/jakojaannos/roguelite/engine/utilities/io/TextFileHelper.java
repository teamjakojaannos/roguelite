package fi.jakojäännös.roguelite.engine.utilities.io;

import lombok.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextFileHelper {
    /**
     * Reads a file's contents to a single string. UTF-8 formatting is assumed.
     *
     * @param path path to read file from
     *
     * @return string containing contents of a file
     *
     * @throws IOException          if opening the file fails
     * @throws NullPointerException if path is null
     */
    @NonNull
    public static String readFileToString(@NonNull String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
