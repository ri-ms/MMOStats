package com.klrir.mmoStats.game;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityAtributes {
    Attributes[] value();

    enum Attributes{
        AbilityImune,
        ProjectileImune,
        FerocityImune,
        MeleeImunity,
        DinnerboneEntity
    }
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MagicResistance {
        double multiplier();
    }
}