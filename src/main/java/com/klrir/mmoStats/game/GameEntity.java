package com.klrir.mmoStats.game;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.elements.Elementable;
import com.klrir.mmoStats.entities.StandCoreExtention;
import com.klrir.mmoStats.utils.runnable.EntityRunnable;
import com.klrir.mmoStats.utils.runnable.SequencedRunnable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.klrir.mmoStats.game.Slayer.getBaseName;

public abstract class GameEntity implements Elementable {

    protected static final HashMap<Entity, StandCoreExtention> coreExtentions = new HashMap<>();
    protected int health;
    protected final Set<StandCoreExtention> extentions = new HashSet<>();
    @Getter
    protected final Set<SequencedRunnable> sequencedRunnables = new HashSet<>();
    public static EntityMap livingEntity = EntityMap.getInstance();
    public static ArrayList<LivingEntity> cooldowns = new ArrayList<>();

    public GameEntity() {
        health = getMaxHealth();
    }

    public int getHealth() {
        if (health > getMaxHealth()) throw new IllegalArgumentException("Health is too large");
        return health;
    }

    public boolean hasNoKB() {
        return false;
    }

    public abstract int getMaxHealth();
    public abstract String getName();
    public abstract LivingEntity getEntity();
    public int getDamage() {
        return  0;
    }

    @Getter
    private boolean hasDoneDeath = false;

    @MustBeInvokedByOverriders
    @OverridingMethodsMustInvokeSuper
    public void kill() {
        hasDoneDeath = true;
        EntityRunnable.remove(this);
        removeExtention();
        sequencedRunnables.forEach(SequencedRunnable::setCancelled);
    }

    private void removeExtention() {
        extentions.forEach(extention -> extention.entity().remove());
    }

    public static void killEntity(@NotNull GameEntity entity, GamePlayer killer) {
        LivingEntity e = entity.getEntity();
        if (killer != null) e.addScoreboardTag("killer:" + killer.getName());
        MMOStats.EntityDeath(e);
        e.damage(9999999, killer);
        e.setHealth(0);
        if (e instanceof EnderDragon) e.setHealth(0);
        if(!entity.hasDoneDeath) entity.kill();
        EntityMap.remove((Entity) e);
    }

    public void updateNameTag() {
        getEntity().customName(getBaseName(this));
    }

    public static void killEntity(@NotNull LivingEntity e, GamePlayer killer) {
        if (EntityMap.exists(e)) {
            killEntity(EntityMap.getGmEntity(e), killer);
            return;
        }

        if (killer != null) e.addScoreboardTag("killer:" + killer.getName());
        MMOStats.EntityDeath(e);
        e.damage(9999999, killer);
        if (e instanceof EnderDragon) e.setHealth(0);
    }

    public static void updateEntity(GameEntity e) {
        LivingEntity entity = e.getEntity();
        if (MMOStats.entitydead.containsKey(entity) && MMOStats.entitydead.get(entity)) return;
        if (coreExtentions.containsKey(e.getEntity())) {
            if (coreExtentions.get(e.getEntity()).owner().hasDoneDeath) e.getEntity().remove();
            return;
        }

        int health;
        int maxhealth;


        maxhealth = e.getMaxHealth();
        health = e.getHealth();

        if (health <= 0) {
            entity.setCustomNameVisible(false);

            entity.setHealth(0);

            return;
        }
        @SuppressWarnings("deprecation") double estimated = ((double) health / (double) maxhealth) * entity.getMaxHealth();
        entity.setHealth(estimated);
        e.updateNameTag();

    }

    public static boolean isOnCooldown(LivingEntity entity) {
        return cooldowns.contains(entity);
    }

    public static void setOnCooldown(LivingEntity entity) {
        cooldowns.add(entity);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(entity);
                System.out.println("Removed " + entity.getName() + " from cooldown");
            }
        }.runTaskLater(MMOStats.getInstance(), 15);

    }
    public static boolean isExtention(Entity e) {
        return coreExtentions.containsKey(e);
    }

    @Nullable
    public static StandCoreExtention getExtention(Entity e) {
        return coreExtentions.get(e);
    }

    public int getTrueDamage() {
        return 0;
    }

    public void damage(double damage, GamePlayer player) {
        health -= (int) damage;
    }
}
