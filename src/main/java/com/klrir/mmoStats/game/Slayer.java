package com.klrir.mmoStats.game;

import com.klrir.mmoStats.Stats;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.HashMap;

@Getter
public abstract class Slayer extends GameEntity {
    private static final HashMap<GamePlayer, Slayer> slayers = new HashMap<>();
    protected final GamePlayer owner;

    public Slayer(GamePlayer player) {
        owner = player;
    }

    public static Slayer getSlayer(GamePlayer player) {
        return slayers.get(player);
    }

    public static boolean hasActiveSlayer(GamePlayer player) {
        return slayers.containsKey(player);
    }

    public abstract LivingEntity getEntity();

    public abstract void spawn(@NotNull Location loc);


    public void updateNametag() {
        getEntity().customName(getBaseName(this));
        getEntity().setCustomNameVisible(true);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void kill() {
        super.kill();
        if (owner != null)
            slayers.remove(owner);
    }

    public abstract String getName();

    public static Component getBaseName(GameEntity slayer) {
        Component dangerIcon = Component.text(Character.toChars(9760)[0] + " ").color(NamedTextColor.RED);
        Component entityName = Component.text(slayer.getName() + " ").color(NamedTextColor.AQUA);
        Component health = Component.text(slayer.getHealth() + " ").color(NamedTextColor.RED);
        Component healthIcon = Component.text(Stats.Health.getSymbol() + " ").color(NamedTextColor.RED);
        return dangerIcon.append(entityName).append(health).append(healthIcon);
    }
}
