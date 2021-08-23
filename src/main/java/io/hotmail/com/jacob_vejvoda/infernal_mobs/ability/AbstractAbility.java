package io.hotmail.com.jacob_vejvoda.infernal_mobs.ability;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractAbility implements Ability {

    private final String name;

    public AbstractAbility(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
