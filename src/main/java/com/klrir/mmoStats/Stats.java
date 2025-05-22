package com.klrir.mmoStats;

import com.klrir.mmoStats.items.ItemType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public enum Stats {
    Health("health", '❤', NamedTextColor.RED, "Health", false, HotPotatoBookStat.Armor, 4, 100),
    Defense("def", '❈', NamedTextColor.GREEN, "Defense", false, HotPotatoBookStat.Armor, 2),
    Inteligence("mana", '✎', NamedTextColor.AQUA, "Inteligence", false, 100),
    Speed("speed", '✦', NamedTextColor.WHITE, "Speed", false, 100, 500),
    Strength("strength", '❁', NamedTextColor.RED, "Strength", true, HotPotatoBookStat.Sword, 2),
    CritDamage("cd", '☠', NamedTextColor.BLUE, "Crit Damage", true, null, 0, 50),
    CritChance("cc", '☣', NamedTextColor.BLUE, "Crit Chance", true, 30),
    AbilityDamage("abilitydamage", '๑', NamedTextColor.RED, "Ability Damage", true),
    AttackSpeed("as", '⚔', NamedTextColor.YELLOW, "Attack Speed", true),
    TrueDefense("truedefense", '❂', NamedTextColor.WHITE, "True Defense", false),
    HealthRegen("healthregen", '❣', NamedTextColor.RED, "Health Regen", false),
    ManaRegen("manaregen", '⚡', NamedTextColor.AQUA, "Mana Regen", false, 0, -1, true),
    Hearts("hearts", '❤', NamedTextColor.RED, "Hearts", false, 10, -1, true),
    WeaponDamage("dmg", ' ', NamedTextColor.RED, "Damage", true, HotPotatoBookStat.Sword, 2),
    Vitality("vitality", '♨', NamedTextColor.RED, "Vitality", false);
    public static final List<Stats> statItemDisplayOrder = List.of(WeaponDamage, Strength, CritChance, CritDamage, AttackSpeed, AbilityDamage, Health, Defense, Speed, Inteligence,
            TrueDefense, HealthRegen, Vitality);
    private final String dataName;
    public final char symbol;
    private final NamedTextColor color;
    private final String name;
    private final boolean agressive;
    private final HotPotatoBookStat hotPotatoBookStat;
    private final int hotPotatoBookStatBoost;
    private final double baseAmount;
    private final double maxAmount;
    private final boolean inRift;

    private String extraDef = "⛨";

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive) {
        this(dataName, symbol, color, name, isAggresive, null, 0);
    }

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive, double baseAmount) {
        this(dataName, symbol, color, name, isAggresive, null, 0, baseAmount, -1);
    }

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive, double baseAmount, double maxAmount) {
        this(dataName, symbol, color, name, isAggresive, null, 0, baseAmount, maxAmount);
    }

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive, @Nullable HotPotatoBookStat hotPotatoBookStat, int hotPotatoBookStatBoost) {
        this(dataName, symbol, color, name, isAggresive, hotPotatoBookStat, hotPotatoBookStatBoost, 0, -1);
    }

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive, @Nullable HotPotatoBookStat hotPotatoBookStat, int hotPotatoBookStatBoost, double baseAmount) {
        this(dataName, symbol, color, name, isAggresive, hotPotatoBookStat, hotPotatoBookStatBoost, baseAmount, -1);
    }

    Stats(String dataName, char symbol, @NotNull NamedTextColor color, String name, boolean isAggresive, @Nullable HotPotatoBookStat hotPotatoBookStat, int hotPotatoBookStatBoost, double baseAmount, double maxAmount) {
        this.dataName = dataName;
        this.symbol = symbol;
        this.color = color;
        this.name = name;
        agressive = isAggresive;
        this.hotPotatoBookStat = hotPotatoBookStat;
        this.hotPotatoBookStatBoost = hotPotatoBookStatBoost;
        this.baseAmount = baseAmount;
        this.maxAmount = maxAmount;
        inRift = false;
    }

    Stats(String dataName, char symbol, NamedTextColor color, String name, boolean isAggresive, int baseAmount, int maxAmount, boolean isInRift) {
        this.dataName = dataName;
        this.symbol = symbol;
        this.color = color;
        this.name = name;
        hotPotatoBookStat = null;
        this.baseAmount = baseAmount;
        hotPotatoBookStatBoost = 0;
        this.maxAmount = maxAmount;
        this.inRift = isInRift;
        this.agressive = isAggresive;
    }

    public String toString() {
        return color.toString() + symbol + " " + name;
    }

    public NamespacedKey getKey() {
        return new NamespacedKey(MMOStats.getInstance(), getDataName());
    }

    public static Stats getFromDataName(String data) {
        for (Stats s : Stats.values())
            if (s.getDataName().equals(data))
                return s;
        throw new IndexOutOfBoundsException("There is no stat with the id: " + data);
    }

    public enum HotPotatoBookStat {
        Sword(getSwordsTypes()),
        Armor(Set.of(ItemType.Helmet, ItemType.Chestplate, ItemType.Leggings, ItemType.Boots));
        private final Set<ItemType> types;

        HotPotatoBookStat(Set<ItemType> types) {
            this.types = types;
        }

        static Set<ItemType> getSwordsTypes() {
            Set<ItemType> types = new HashSet<>(List.of(ItemType.values()));
            types.remove(ItemType.Helmet);
            types.remove(ItemType.Chestplate);
            types.remove(ItemType.Leggings);
            types.remove(ItemType.Boots);
            return types;
        }
        public boolean contains(ItemType type) {
            return types.contains(type);
        }
    }
}