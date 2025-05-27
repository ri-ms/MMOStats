package com.klrir.mmoStats.transformation;

import com.klrir.mmoStats.Stats;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Sunshine extends Transformation{

    @Override
    public Map<Stats, Double> getBuffs() {
        return Map.of(
                Stats.Strength, 1.25
        );
    }

    @Override
    public String getName() {
        return "Sunshine";
    }
}
