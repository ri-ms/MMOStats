package com.klrir.mmoStats.utils.inventories;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface InventoryTemplate {

    @Range(from = 1,to = 6)
    int getRows();
    @NotNull
    Inventory getCompiledItems();
    @NotNull
    String getName();
}