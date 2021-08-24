package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Abilities {

    public static final Ability ONE_UP = new OneUpAbility();
    public static final Ability BLINDING = new BlindingAbility();
    public static final Ability BULWARK = new BulwarkAbility();
    public static final Ability CONFUSE = new ConfuseAbility();
    public static final Ability LIFE_STEAL = new LifeSteal();
    public static final Ability QUICKSAND = new QuicksandAbility();
    public static final Ability RUST = new RustAbility();
    public static final Ability SAPPER = new SapperAbility();
    public static final Ability WITHERING = new WitheringAbility();

    public static @Nullable Ability fromName(@NotNull String name) {
        return switch (name) {
            case "1up" -> ONE_UP;
            case "blinding" -> BLINDING;
            // bullwark is probably a typo, but it is used in the configuration, so we'll keep it for compatibility...
            case "bulwark", "bullwark" -> BULWARK;
            case "confuse" -> CONFUSE;
            case "lifesteal" -> LIFE_STEAL;
            case "quicksand" -> QUICKSAND;
            case "rust" -> RUST;
            case "sapper" -> SAPPER;
            case "withering" -> WITHERING;
            default -> null;
        };
    }
}
