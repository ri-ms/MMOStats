package com.klrir.mmoStats.transformation;

import com.klrir.mmoStats.Stats;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class Transformation {

    public abstract Map<Stats, Double> getBuffs();
    public abstract String getName();

    private static Map<Transformation, Map<Stats, Double>> transformations;
    private static Map<Player, Transformation> transformedPlayers;

    public boolean isPlayerTransformed(Player player) {
        return transformedPlayers.containsKey(player);
    }

    public boolean setPlayerTransformed(Player player, Transformation transformation) {
        if (isPlayerTransformed(player)){
            return true;
        }
        transformedPlayers.put(player, transformation);
        return false;
    }

    public static void registerTransformation(Transformation transformation) {
        transformations.put(transformation, transformation.getBuffs());
    }
}
