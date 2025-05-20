package com.klrir.mmoStats.items;

import lombok.Getter;

public enum ItemRarity {
    UNDEFINED("§f§lUNDEFINED", "§f"),
    COMMON("§f§lCOMMON", "§f"),
    UNCOMMON("§a§lUNCOMMON", "§a"),
    RARE("§9§lRARE", "§9"),
    EPIC("§5§lEPIC", "§5"),
    LEGENDARY("§6§lLEGENDARY", "§6"),
    MYTHIC("§d§lMYTHIC", "§d"),
    DIVINE("§b§lDIVINE", "§b"),
    ADMIN("§4§lADMIN", "§4");

    private final String name;
    @Getter
    private final String prefix;

    ItemRarity(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }
    public String getRarityName() {
        return name;
    }

    public ItemRarity getNext() {
        return switch (this) {
            case COMMON -> UNCOMMON;
            case UNCOMMON -> RARE;
            case RARE -> EPIC;
            case EPIC -> LEGENDARY;
            case LEGENDARY -> MYTHIC;
            case MYTHIC -> DIVINE;
            case ADMIN -> ADMIN;
            default -> UNDEFINED;
        };
    }

    public ItemRarity getBefore() {
        return switch (this) {
            case ADMIN -> ADMIN;
            case DIVINE -> MYTHIC;
            case MYTHIC -> LEGENDARY;
            case LEGENDARY -> EPIC;
            case EPIC -> RARE;
            case RARE -> UNCOMMON;
            case COMMON -> COMMON;
            case UNCOMMON -> COMMON;
            default -> UNDEFINED;
        };
    }
}
