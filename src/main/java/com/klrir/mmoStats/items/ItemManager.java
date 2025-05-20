package com.klrir.mmoStats.items;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import com.klrir.mmoStats.utils.NBTEditor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ItemManager implements ItemGenerator{
    public static String pattern = "MMMMMMMMM yyyy";
    public static SimpleDateFormat df = new SimpleDateFormat(pattern);
    public String name;
    @Setter
    public List<String> lore = new ArrayList<>();
    public String itemID;
    public boolean isDungeonItem = false;
    public void setDungeonItem(boolean b) {
        isDungeonItem = b;
        if (maxStars == 0) maxStars = 5;
    }
    public ItemType type;
    public Material material;
    @Getter
    @Setter
    private int maxStars = 0;
    public boolean isHead;
    public String headTexture;
    private boolean isSkullValue = false;
    public UUID customUUID;
    private boolean isUnstackeble = false;
    private HashMap<Stats, Double> stats = new HashMap<>();
    public int baseabilitydamage;
    public HashMap<Enchantment, Integer> enchants = new HashMap<>();
    public HashMap<String, String> customDataContainer = new HashMap<>();
    public HashMap<String, Integer> customIntContainer = new HashMap<>();
    public HashMap<String, Double> customDoubleContainer = new HashMap<>();
    private ItemRarity rarity;
    public Color color;
    public boolean isBaseItem = false;
    @Getter
    @Setter
    private SpecialRarityGrabber rarityGrabber = new SpecialRarityGrabber() {
        @Override
        public ItemRarity getRarity(@NotNull ItemRarity rarity, @NotNull ItemStack item, @Nullable GamePlayer player) {
            return rarity;
        }
    };
    @Override
    public ItemStack createNewItemStack() {
        ItemStack item = new ItemStack(material);

        if (color != null) {
            LeatherArmorMeta lmeta = (boolean) item.hasItemMeta() ? (LeatherArmorMeta) item.getItemMeta() : (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(item.getType());
            lmeta.setColor(color);
            lmeta.addItemFlags(ItemFlag.HIDE_DYE);
            item.setItemMeta(lmeta);
        }

        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        meta.addItemFlags(ItemFlag.HIDE_DYE);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        meta.displayName(MiniMessage.miniMessage().deserialize(rarity.getPrefix() + name));
        if (enchants != null) enchants.forEach((enchant, level) -> {
            meta.addEnchant(enchant, level, true);
        });
        data.set(new NamespacedKey(MMOStats.getInstance(), "id"), PersistentDataType.STRING, itemID);
        data.set(new NamespacedKey(MMOStats.getInstance(), "recomed"), PersistentDataType.INTEGER, 0);
        ArrayList<String> lore = new ArrayList<>();
        if (this.lore != null)
            lore.addAll(this.lore);

        if (isUnstackeble)
            data.set(new NamespacedKey(MMOStats.getInstance(), "uuid"), PersistentDataType.STRING, UUID.randomUUID().toString());
        lore.add(" ");
        lore.add(rarity.getRarityName() + type.toString());
        meta.lore(getLore(lore));

        if (customIntContainer != null) {
            customIntContainer.forEach((arg1, arg2) -> {
                data.set(new NamespacedKey(MMOStats.getInstance(), arg1), PersistentDataType.INTEGER, arg2);
            });
        }
        if (customDataContainer != null) {
            customDataContainer.forEach((arg1, arg2) -> {
                data.set(new NamespacedKey(MMOStats.getInstance(), arg1), PersistentDataType.STRING, arg2);
            });
        }
        lore.add(" ");
        lore.add(rarity.getRarityName() + type.toString());
        meta.lore(getLore(lore));
        meta.setUnbreakable(true);
        item.setItemMeta(meta);

        NBTEditor.set(item, itemID, "ExtraAttributes", "id");
        return item;
    }
    public double getStat(@NotNull Stats stat) {
        return stats.getOrDefault(stat, 0d);
    }

    public ItemStack getRawItemStack() {
        ItemStack item = new ItemStack(material);

        if (color != null) {
            LeatherArmorMeta lmeta = (boolean) item.hasItemMeta() ? (LeatherArmorMeta) item.getItemMeta() : (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(item.getType());
            lmeta.setColor(color);
            item.setItemMeta(lmeta);
        }

        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        meta.displayName(MiniMessage.miniMessage().deserialize(rarity.getPrefix() + name));
        if (enchants != null) enchants.forEach((enchant, level) -> {
            meta.addEnchant(enchant, level, true);
        });
        data.set(new NamespacedKey(MMOStats.getInstance(), "id"), PersistentDataType.STRING, itemID);
        data.set(new NamespacedKey(MMOStats.getInstance(), "recomed"), PersistentDataType.INTEGER, 0);
        if (isUnstackeble)
            data.set(new NamespacedKey(MMOStats.getInstance(), "uuid"), PersistentDataType.STRING, UUID.randomUUID().toString());
        ArrayList<String> lore = new ArrayList<>();
        if (this.lore != null) lore.addAll(this.lore);

        if (customDataContainer != null) {
            customDataContainer.forEach((arg1, arg2) -> {
                data.set(new NamespacedKey(MMOStats.getInstance(), arg1), PersistentDataType.STRING, arg2);
            });
        }
        lore.add("");
        lore.add(rarity.getRarityName() + type.toString());
        meta.lore(getLore(lore));
        meta.setUnbreakable(true);
        if (customIntContainer != null) {
            customIntContainer.forEach((arg1, arg2) -> {
                data.set(new NamespacedKey(MMOStats.getInstance(), arg1), PersistentDataType.INTEGER, arg2);
            });
        }
        item.setItemMeta(meta);
        return item;
    }

    private static List<Component> getLore(ArrayList<String> lore) {
        List<Component> loreComponents = lore.stream()
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .toList();
        return loreComponents;
    }

    @Deprecated
    public ItemRarity getRarity() {
        return rarity;
    }

    public ItemRarity getRarity(@NotNull ItemStack item) {
        return getRarity(item, null);
    }

    public ItemRarity getRarity(@NotNull ItemStack item, GamePlayer player) {
        ItemRarity r = rarityGrabber.getRarity(rarity, item, player);
        if (ItemHandler.getOrDefaultPDC("recomed", item, PersistentDataType.INTEGER, 0) == 1) r = r.getNext();
        return r;
    }

    public static interface SpecialRarityGrabber {
        ItemRarity getRarity(@NotNull ItemRarity rarity, @NotNull ItemStack item, @Nullable GamePlayer player);
    }

}
