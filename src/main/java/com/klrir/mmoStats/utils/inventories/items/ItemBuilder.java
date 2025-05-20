package com.klrir.mmoStats.utils.inventories.items;

import com.klrir.mmoStats.items.enchantments.GameEnchants;
import com.klrir.mmoStats.utils.runnable.Return;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemBuilder {
    private final Material material;
    private String name = "N/A";
    private final ArrayList<String> lore = new ArrayList<>();
    private final List<ItemFlag> flags = new ArrayList<>();
    private int count = 1;
    private boolean isHead;
    private String headURL;
    private Color color;
    private ArrayList<Pattern> bannerPatterns;
    private final HashMap<Enchantment, Integer> enchants = new HashMap<>();
    private boolean headvalue = false;
    public ItemBuilder(Material material){
        this.material = material;
    }
    public ItemBuilder setName(String str){
        this.name = str;
        return this;
    }
    public ItemBuilder addLoreRow(String str){
        return setLoreRow(lore.size(), str);
    }
    public ItemBuilder setLoreRow(int i, String str){
        if(i == lore.size())
            lore.add(str);
        else
            lore.set(i,str);

        return this;
    }
    public ItemBuilder addAllLore(List<String> lore){
        for(String l : lore)
            addLoreRow(l);
        return this;
    }

    public ItemBuilder addLoreIf(Return<Boolean> predicate, String... lore) {
        if (!predicate.run()) return this;
        for(String l : lore)
            addLoreRow(l);
        return this;
    }

    public ItemBuilder addAllLore(String... lore){
        for(String l : lore)
            addLoreRow(l);
        return this;
    }

    public ItemBuilder addAllLore(TextColor base, String... lore){
        for(String l : lore)
            addLoreRow(base + l);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag){
        flags.add(flag);
        return this;
    }
    public ItemBuilder setAmount(int i){
        count = i;
        return this;
    }
    public ItemStack build(){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (!name.equals("N/A"))
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            List<Component> loreComponents = lore.stream()
                    .map(line -> MiniMessage.miniMessage().deserialize(line))
                    .toList();
            meta.lore(loreComponents);
        for(ItemFlag itemFlag : flags)
            meta.addItemFlags(itemFlag);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        for (Enchantment enchantment : enchants.keySet()){
            meta.addEnchant(enchantment, enchants.get(enchantment), true);
        }
        item.setItemMeta(meta);
        if(color != null){
            LeatherArmorMeta lmeta = item.hasItemMeta() ? (LeatherArmorMeta) item.getItemMeta() : (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(item.getType());
            lmeta.setColor(color);
            lmeta.addItemFlags(ItemFlag.HIDE_DYE);
            item.setItemMeta(lmeta);
        }
        if(bannerPatterns != null){

            BannerMeta lmeta = (BannerMeta) item.getItemMeta();
            lmeta.setPatterns(bannerPatterns);
            item.setItemMeta(lmeta);
        }
        item.setAmount(count);
        return item;
    }

    public ItemBuilder setHead(String url){
        isHead = true;
        headURL = url;
        return this;
    }
    public ItemBuilder headTextureAsValue() {
        headvalue = true;
        return this;
    }
    public ItemBuilder setLeatherColor(Color color){
        this.color = color;
        return this;
    }
    public ItemBuilder setBannerPatterns(ArrayList<Pattern> patterns){
        bannerPatterns = patterns;
        return this;
    }
    public ItemBuilder addEnchant(Enchantment enchantment, int level){
        enchants.put(enchantment, level);
        return this;
    }
}