package fi.jakojaannos.roguelite.engine.content;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class AssetHandle {
    public static final String DEFAULT_DOMAIN = "konna";

    @Getter private final String domain;
    @Getter private final String name;

    public AssetHandle(final String name) {
        this(DEFAULT_DOMAIN, name);
    }

    public AssetHandle(final String domain, final String name) {
        this.domain = domain;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", this.domain, this.name);
    }
}
