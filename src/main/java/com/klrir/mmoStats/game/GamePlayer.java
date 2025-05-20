package com.klrir.mmoStats.game;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.API.PlayerEvent.GetTotalStatEvent;
import com.klrir.mmoStats.API.PlayerEvent.PlayerHealthChangeEvent;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.configs.ConfigFile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.swing.plaf.synth.Region;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class GamePlayer extends CraftPlayer {
    private static HashMap<Player, GamePlayer> players = new HashMap<>();
    private final Player player;
    public double healingMulti = 1;
    public int currhealth;
    public double currmana;
    public int absorbtion;
    @Setter
    public int speedCap = 500;
    private final ConfigFile inventory;
    @Getter
    private int magicalpower = 0;
    @Getter
    @Setter
    private double addictiveMultiplier = 1;
    @Getter
    private final SortedSet<Effect> activeEffects = new TreeSet<>((o1, o2) -> o1.name().compareTo(o2.name()));
    private final ConfigFile statsConfig = new ConfigFile(this, "stats");
    public String defenseString = "";
    public boolean showDefenceString = false;
    private BukkitRunnable defenceStringRunnable;
    private HashMap<Stats, Double> baseStat = new HashMap<>();
    @Getter
    private double manaRegenMult = 1;
    private boolean hasDeployableEffect = false;
    @Getter
    private double rawDamageMult = 1;
    @Getter
    @Setter
    private Region region;

    public GamePlayer(CraftServer server, ServerPlayer player){
        this(server, player, false);
    }
    public GamePlayer(CraftServer server, ServerPlayer entity, boolean isChecked) {
        super(server, entity);
        inventory = new ConfigFile(this, "inventory");
        this.player = entity.getBukkitEntity().getPlayer();
        players.put(player, this);
        for (Stats stat : Stats.values()){
            if (stat == Stats.WeaponDamage) continue;
            double value = statsConfig.get().getDouble(stat.getDataName(), stat.getBaseAmount());
            if (stat.getMaxAmount() > 0 && value > stat.getMaxAmount()) value = stat.getMaxAmount();
            setBaseStat(stat, value);
        }

        loadInventory();
        currmana = (int) MMOStats.getPlayerStat(this, Stats.Inteligence);
        currhealth = (int) MMOStats.getPlayerStat(this, Stats.Health);
    }

    public void unregister() {
        for (Stats stats : Stats.values()) {
            if (stats == Stats.WeaponDamage) continue;
            statsConfig.get().set(stats.getDataName(), getBaseStat(stats));
        }
        statsConfig.save(MMOStats.getInstance().isEnabled());
    }

    private void loadInventory() {
        Bukkit.getScheduler().runTaskAsynchronously(MMOStats.getInstance(), () -> {
            getInventory().clear();
            inventory.reload();
            try {
                if (inventory.get().getConfigurationSection("") != null) {
                    for (String k : inventory.get().getKeys(false)) {
                        int i = Integer.parseInt(k);
                        ItemStack item = inventory.get().getItemStack(k);
                        new BukkitRunnable() {
                            public void run() {
                                //MOStats.itemUpdater(item, GamePlayer.this);
                                player.getInventory().setItem(i, item);
                            }
                        }.runTask(MMOStats.getInstance());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.updateInventory();
        });
    }

    private void saveInventory0() {
        inventory.clear();
        inventory.save();
        inventory.reload();
        for (int i = 0; i < 40; i++) {
            ItemStack item = this.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR)
                inventory.get().set(i + "", this.getInventory().getItem(i));
        }
        inventory.save();
        inventory.reload();
    }

    private HashMap<String, String> getItemAsMap(ItemStack item) {
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        HashMap<String, String> map = new HashMap<>();
        for (NamespacedKey key : data.getKeys()) {
            if (data.has(key, PersistentDataType.STRING))
                map.put(key.getKey(), data.get(key, PersistentDataType.STRING));
            else if (data.has(key, PersistentDataType.DOUBLE))
                map.put(key.getKey(), data.get(key, PersistentDataType.DOUBLE).toString());
            else if (data.has(key, PersistentDataType.FLOAT))
                map.put(key.getKey(), data.get(key, PersistentDataType.FLOAT).toString());
            else if (data.has(key, PersistentDataType.INTEGER))
                map.put(key.getKey(), data.get(key, PersistentDataType.INTEGER).toString());

        }
        return map;
    }

    private void saveItem(int Pointer, HashMap<String, String> bundle) {
        bundle.forEach((t, k) -> inventory.get().set(getUniqueId() + ".SLOT_" + Pointer + ".pdc." + t, k));
    }

    public double getBaseStat(Stats stat) {
        return baseStat.getOrDefault(stat, 0d);
    }

    public void setBaseStat(Stats stat, double value) {
        baseStat.put(stat, value);
    }
    public ItemStack getItemInHand(){
        return this.player.getInventory().getItemInMainHand();
    }

    public synchronized double getStat(Stats stat) {
        double value = this.getBaseStat(stat);
        GetTotalStatEvent event = new GetTotalStatEvent(this, stat, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return stat.getBaseAmount();
        value = event.getValue() * event.getMultiplier();
        return value;
    }

    public static GamePlayer getGamePlayer(Player player){
        return players.get(player);
    }

    public void setMana(double value) {
        currmana = value;
        double max = MMOStats.getPlayerStat(this, Stats.Inteligence);
        if (currmana > max) currmana = max;
    }

    private void hideDefenceString() {
        try {
            defenceStringRunnable.cancel();
        } catch (Exception ignored) {

        }
        defenceStringRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                showDefenceString = false;
                MMOStats.updatebar(GamePlayer.this);

            }
        };
        defenceStringRunnable.runTaskLater(MMOStats.getInstance(), 20 * 3);
    }

    public void setTempDefenceString(String str) {
        defenseString = str;
        showDefenceString = true;
        MMOStats.updatebar(this);
        hideDefenceString();
    }

    public void setHealingMult(double d) {
        healingMulti = d;

    }
    public void addAddictiveMultiplier(double value) {
        addictiveMultiplier += value;
    }

    public void addHealingMult(double d) {
        setHealingMult(d += healingMulti);
    }

    @Deprecated
    public void setHealth(int value) {
        setHealth(value, HealthChangeReason.Creative);
    }

    @Deprecated
    public void setHealth(double value) {
        setHealth(value, HealthChangeReason.Creative);
    }

    @Deprecated
    public void setHealth(float value) {
        setHealth(value, HealthChangeReason.Creative);
    }

    public void setHealth(int value, HealthChangeReason reason) {
        setHealth((double) value, reason);

    }

    public void setHealth(double value, HealthChangeReason reason) {
        if (reason != HealthChangeReason.Force) {
            PlayerHealthChangeEvent event = new PlayerHealthChangeEvent(this, (int) (currhealth - value), reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            currhealth = currhealth - event.getHealthChangeAmount();
        } else currhealth = (int) value;
        if (currhealth < 0) currhealth = 0;
        double maxHealth = MMOStats.getPlayerStat(this, Stats.Health);
        if (currhealth > maxHealth) currhealth = (int) maxHealth;
        MMOStats.updatebar(this);
    }

    public void setHealth(float value, HealthChangeReason reason) {
        setHealth((double) value, reason);
    }

    public void kill() {
        Slayer possibleSlayer = Slayer.getSlayer(this);
        if (possibleSlayer != null) GameEntity.killEntity(possibleSlayer, null);
    }
}
