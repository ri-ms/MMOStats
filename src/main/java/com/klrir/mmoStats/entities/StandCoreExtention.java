package com.klrir.mmoStats.entities;

import com.klrir.mmoStats.game.GameEntity;
import org.bukkit.entity.Entity;

public record StandCoreExtention(Entity entity, GameEntity owner) {
}
