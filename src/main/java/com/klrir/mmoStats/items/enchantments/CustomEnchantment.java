package com.klrir.mmoStats.items.enchantments;

import com.klrir.mmoStats.items.ItemType;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomEnchantment extends Enchantment {
    @Override
    public final boolean isTreasure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final boolean isCursed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment arg0) {
        if (arg0 instanceof CustomEnchantment ce) return conflictEnchants().contains(ce);
        return false;
    }

    public List<CustomEnchantment> conflictEnchants() {
        return new ArrayList<>();
    }

    public abstract ItemType[] getAllowedTypes();

    public static CustomEnchantment toCustomEnchantment(Enchantment enchantment) {
        if (enchantment instanceof CustomEnchantment ce) return ce;
        return GameEnchants.registeredEnchants.get(enchantment.getKey().getKey());
    }

}