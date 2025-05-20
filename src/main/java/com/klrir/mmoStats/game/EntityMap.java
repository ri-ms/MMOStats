package com.klrir.mmoStats.game;

import com.klrir.mmoStats.entities.BasicEntity;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityMap {
    private static final Map<Entity, GameEntity> allEntities = new HashMap<>();
    @Getter
    private static EntityMap instance;

    public static void addEntity(Entity key, GameEntity value) {
        allEntities.put(key, value);
    }

    public static boolean exists(Entity key) {
        return allEntities.containsKey(key);
    }

    public static void remove(Entity key) {
        allEntities.remove(key);
    }

    public static GameEntity getGmEntity(Entity entity) {
        GameEntity e = allEntities.get(entity);
        if (e == null) {
            return new BasicEntity((LivingEntity) entity);
        }
        return e;
    }

    public static Collection<GameEntity> getSbEntities() {
        return allEntities.values();
    }
}
