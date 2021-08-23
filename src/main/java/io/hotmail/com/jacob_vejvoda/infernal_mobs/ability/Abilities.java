package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Abilities {

    public static final Ability ONE_UP = new OneUpAbility();

    public static @Nullable Ability fromName(@NotNull String name) {
        return switch (name) {
            case "1up" -> ONE_UP;
            default -> null;
        };
    }
}
