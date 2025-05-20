package com.klrir.mmoStats.elements;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

public enum Element {
    Pyro("\uD83D\uDD25", NamedTextColor.RED),
    Cryo("❆", NamedTextColor.AQUA),
    Hydro("\uD83C\uDF0A", NamedTextColor.BLUE),
    Geo("✧", NamedTextColor.GOLD),
    Electro("⚡", NamedTextColor.DARK_PURPLE);

    @Getter
    private final String symbol;
    @Getter
    private final NamedTextColor color;

    Element(String sign, NamedTextColor chatColor) {
        symbol = sign;
        color = chatColor;
    }
}