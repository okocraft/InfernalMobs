package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Abilities implements Ability {
    ARMOURED(new ArmouredAbility()),
    BLINDING(new BlindingAbility()),
    BULWARK(new BulwarkAbility()),
    CONFUSE(new ConfuseAbility()),
    LIFE_STEAL(new LifeSteal()),
    ONE_UP(new OneUpAbility()),
    QUICKSAND(new QuicksandAbility()),
    RUST(new RustAbility()),
    SAPPER(new SapperAbility()),
    WITHERING(new WitheringAbility()),
    ;

    public static @Nullable Ability fromName(@NotNull String name) {
        return switch (name) {
            case "1up" -> ONE_UP;
            case "armoured" -> ARMOURED;
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

    private final Ability ability;

    Abilities(@NotNull Ability ability) {
        this.ability = ability;
    }

    @Override
    public @NotNull String getName() {
        return ability.getName();
    }

    @Override
    public boolean isApplicable(@NotNull Mob mob) {
        return ability.isApplicable(mob);
    }

    @Override
    public void onSpawn(@NotNull Mob infernalMob) {
        ability.onSpawn(infernalMob);
    }

    @Override
    public void attack(@NotNull Mob infernalMob, @NotNull Mob target) {
        ability.attack(infernalMob, target);
    }

    @Override
    public void counterattack(@NotNull Mob infernalMob, @NotNull Mob target) {
        ability.counterattack(infernalMob, target);
    }

    @Override
    public void showEffect(@NotNull Mob infernalMob) {
        ability.showEffect(infernalMob);
    }

    @Override
    public void onDamaged(@NotNull Mob infernalMob, double damageAmount) {
        ability.onDamaged(infernalMob, damageAmount);
    }

    @Override
    public void onDamaged(@NotNull Mob infernalMob, @NotNull Mob damager, double damageAmount) {
        ability.onDamaged(infernalMob, damager, damageAmount);
    }

    @Override
    public void onKilled(@NotNull Mob infernalMob, @NotNull Mob killer) {
        ability.onKilled(infernalMob, killer);
    }
}
