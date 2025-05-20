package com.klrir.mmoStats.utils.runnable;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.game.GameEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class EntityRunnable extends BukkitRunnable {
    public static final HashMap<GameEntity, Set<EntityRunnable>> runnable = new HashMap<>();
    private GameEntity entity;

    @NotNull
    public synchronized BukkitTask runTaskTimer(@NotNull GameEntity entity, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        addToMap(entity);
        return super.runTaskTimer(MMOStats.getInstance(), delay, period);
    }

    @NotNull
    public synchronized BukkitTask runTaskLater(@NotNull GameEntity entity, long delay) throws IllegalArgumentException, IllegalStateException {
        addToMap(entity);
        return super.runTaskLater(MMOStats.getInstance(), delay);
    }

    @NotNull
    public synchronized BukkitTask runTaskTimerAsynchronously(@NotNull GameEntity entity, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        addToMap(entity);
        return super.runTaskTimerAsynchronously(MMOStats.getInstance(), delay, period);
    }

    @NotNull
    public synchronized BukkitTask runTaskLaterAsynchronously(@NotNull GameEntity entity, long delay) throws IllegalArgumentException, IllegalStateException {
        addToMap(entity);
        return super.runTaskLaterAsynchronously(MMOStats.getInstance(), delay);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        if (entity != null) {
            if (runnable.containsKey(entity)) {
                Set<EntityRunnable> entityRunnables = runnable.get(entity);
                entityRunnables.remove(this);
                if (entityRunnables.isEmpty()) runnable.remove(entity);
                else runnable.put(entity, entityRunnables);
            }
        }
        super.cancel();
    }

    private void addToMap(GameEntity entity) {
        this.entity = entity;
        Set<EntityRunnable> entityRunnables;
        if (runnable.containsKey(entity)) entityRunnables = runnable.get(entity);
        else entityRunnables = new HashSet<>();
        entityRunnables.add(this);
        runnable.put(entity, entityRunnables);
    }

    public static void remove(GameEntity entity) {
        if (!runnable.containsKey(entity)) return;
        Set<EntityRunnable> entityRunnables = runnable.get(entity);
        runnable.remove(entity);
        for (EntityRunnable r : entityRunnables) {
            try {
                r.cancel();
            } catch (Exception ignored) {}
        }

    }
}
