package com.klrir.mmoStats.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.klrir.mmoStats.API.Bundle;
import com.klrir.mmoStats.API.CalculatorException;
import com.klrir.mmoStats.API.GameDamageEvent;
import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.API.PlayerEvent.DamagePrepairEvent;
import com.klrir.mmoStats.API.PlayerEvent.GameDamagePlayerToEntityExecuteEvent;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.elements.Element;
import com.klrir.mmoStats.elements.Elementable;
import com.klrir.mmoStats.entities.BasicEntity;
import com.klrir.mmoStats.entities.EntityHandler;
import com.klrir.mmoStats.hook.Hook;
import com.klrir.mmoStats.utils.Tools;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public class Calculator {
    public boolean isCrit = false;
    private boolean isCanceled = false;
    public int cccalc = 0;
    public double damage = 0;
    @Getter
    private GameDamageEvent.DamageType type;
    private LivingEntity e;
    private Projectile projectile;
    private GameDamageEvent result;
    private boolean isMagic = false;
    private String abilityName;
    private double abilityScaling = 1;
    @Getter
    @Setter
    private Element element;
    @Getter
    private List<String> tags = new ArrayList();
    @Getter
    @Setter
    private boolean ignoreHit = false;
    @Getter
    private GameEntity gameEntity;

    public Calculator() {
    }

    public Calculator(Projectile p) {
        projectile = p;
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player) {
        return playerToEntityDamage(e, player, new Bundle<>(1d, 1d));
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player, Bundle<Double, Double> multipyers) {
        return playerToEntityDamage(e, player, new HashMap<>(), multipyers, true);
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player, HashMap<Stats, Double> stats, boolean fillMissing) {
        return playerToEntityDamage(e, player, stats, new Bundle<>(1d, 1d), fillMissing);
    }

    public HashMap<Stats, Double> getIfMissing(HashMap<Stats, Double> stats, GamePlayer player) {
        if (!stats.containsKey(Stats.Strength)) stats.put(Stats.Strength, MMOStats.getPlayerStat(player, Stats.Strength));

        if (!stats.containsKey(Stats.CritChance))
            stats.put(Stats.CritChance, MMOStats.getPlayerStat(player, Stats.CritChance));

        if (!stats.containsKey(Stats.CritDamage))
            stats.put(Stats.CritDamage, MMOStats.getPlayerStat(player, Stats.CritDamage));

        return stats;
    }

    public HashMap<Stats, Double> fillWithNull(HashMap<Stats, Double> stats) {
        if (!stats.containsKey(Stats.Strength)) stats.put(Stats.Strength, 0d);

        if (!stats.containsKey(Stats.CritChance)) stats.put(Stats.CritChance, 0d);

        if (!stats.containsKey(Stats.CritDamage)) stats.put(Stats.CritDamage, 0d);

        return stats;
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player, double weapondamage) {
        return playerToEntityDamage(e, player, new HashMap<>(), MMOStats.getItemStat(player, Stats.WeaponDamage, player.getEquipment().getItemInMainHand()), new Bundle<>(1d, 1d), true);
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player, HashMap<Stats, Double> stats, Bundle<Double, Double> multipliers, boolean fillMissing) {
        return playerToEntityDamage(e, player, stats, MMOStats.getItemStat(player, Stats.WeaponDamage, player.getEquipment().getItemInMainHand()), multipliers, fillMissing);
    }

    public double playerToEntityDamage(LivingEntity e, GamePlayer player, HashMap<Stats, Double> stats, double weapondamage, Bundle<Double, Double> multipliers, boolean fillMissing) {
        if (EntityMap.exists(e)) {
            EntityAtributes atributes = EntityMap.getGmEntity(e).getClass().getAnnotation(EntityAtributes.class);
            if (atributes != null) for (EntityAtributes.Attributes attributes : atributes.value()) {
                if (attributes == EntityAtributes.Attributes.MeleeImunity) return 0;
                if (attributes == EntityAtributes.Attributes.ProjectileImune && projectile != null) return 0;
            }
        }
        this.e = e;
        if (e.getScoreboardTags().contains("npc")) return 0d;
        type = GameDamageEvent.DamageType.PlayerToEntity;

        double weapondmg = weapondamage;
        weapondmg *= player.getRawDamageMult();
        if (fillMissing) stats = getIfMissing(stats, player);
        else stats = fillWithNull(stats);
        GameEntity entity = EntityMap.getGmEntity(e);
        gameEntity = entity;
        DamagePrepairEvent event = new DamagePrepairEvent(player, e, this, multipliers.getFirst(), multipliers.getLast(), stats, weapondamage);
        event.addPreMultiplier(GamePlayer.getGamePlayer(player).getAddictiveMultiplier() - 1);
        Bukkit.getPluginManager().callEvent(event);
        weapondmg = event.getWeaponDamage();
        stats = event.getStats();
        double stre = stats.get(Stats.Strength);
        double cd = stats.get(Stats.CritDamage);
        double cc = stats.get(Stats.CritChance);
        cccalc = (int) (Math.random() * 100 + 1);

        double preMultiplier = event.getPreMultiplier();
        double postMult = event.getPostMultiplier();

        double damage;
        if (cccalc <= cc) {
            isCrit = true;
            damage = (5 + (double) weapondmg) * (1 + ((double) stre / 100)) * (1 + ((double) cd / 100)) * ((preMultiplier)) * ((postMult));

        } else {
            damage = (5 + (double) weapondmg) * (1 + ((double) stre / 100)) * ((preMultiplier)) * ((postMult));
        }
        if (EntityMap.exists(e)) {
            if (GameEntity.isExtention(entity.getEntity()))
                entity = GameEntity.getExtention(entity.getEntity()).owner();
            if (entity instanceof Defensive ed) {
                double defense = ed.getDefense();
                double ehp = entity.getMaxHealth() * (1 + (defense / 100));
                double effectivedmg = entity.getMaxHealth() / ehp;
                damage *= effectivedmg;
            }
        }
        this.damage = damage;
        return damage;
    }


    //bundle has Damage - True Damage
    public void entityToPlayerDamage(GameEntity entity, GamePlayer player) {
        entityToPlayerDamage(entity, player, new Bundle<>(entity.getDamage(), entity.getTrueDamage()));
    }

    public void entityToPlayerDamage(GameEntity entity, GamePlayer player, Bundle<Integer, Integer> stats) {
        if (entity != null) e = entity.getEntity();
        gameEntity = entity;
        type = GameDamageEvent.DamageType.EntityToPlayer;
        double damage = stats.getFirst();
        double health = MMOStats.getPlayerStat(player, Stats.Health);
        double defense = MMOStats.getPlayerStat(player, Stats.Defense);
        float ehp = (float) health * (1 + ((float) defense / 100));
        float effectivedmg = (float) health / ehp;
        int totaldmg = (int) ((int) damage * effectivedmg);


        int truedamage = stats.getLast();
        if (truedamage != 0) {
            float trueehp = (float) health * (1 + ((float) MMOStats.getPlayerStat(player, Stats.TrueDefense) / 100));
            float effectivetruedmg = (float) health / trueehp;
            totaldmg += (int) (truedamage * effectivetruedmg);
        }
        this.damage = totaldmg;
    }

    public void playerToPlayerDamage(GamePlayer target, GamePlayer player) {
        playerToPlayerDamage(target, player, new Bundle<>(1D, 1D));
    }

    public double playerToPlayerDamage(GamePlayer target, GamePlayer player, Bundle<Double, Double> multipyers) {
        return playerToPlayerDamage(target, player, new HashMap<>(), multipyers, true);
    }

    public double playerToPlayerDamage(GamePlayer e, GamePlayer player, HashMap<Stats, Double> stats, Bundle<Double, Double> multipliers, boolean fillMissing) {
        return playerToPlayerDamage(e, player, stats, MMOStats.getItemStat(player, Stats.WeaponDamage, player.getEquipment().getItemInMainHand()), multipliers, fillMissing);
    }

    public double playerToPlayerDamage(GamePlayer e, GamePlayer player, HashMap<Stats, Double> stats, double weapondamage, Bundle<Double, Double> multipliers, boolean fillMissing) {

        type = GameDamageEvent.DamageType.PlayerToPlayer;

        double weapondmg = weapondamage;
        weapondmg *= player.getRawDamageMult();
        if (fillMissing) stats = getIfMissing(stats, player);
        else stats = fillWithNull(stats);

        DamagePrepairEvent event = new DamagePrepairEvent(player, e, this, multipliers.getFirst(), multipliers.getLast(), stats, weapondamage);

        event.addPreMultiplier(GamePlayer.getGamePlayer(player).getAddictiveMultiplier() - 1);

        Bukkit.getPluginManager().callEvent(event);
        weapondmg = event.getWeaponDamage();
        stats = event.getStats();
        double stre = stats.get(Stats.Strength);
        double cd = stats.get(Stats.CritDamage);
        double cc = stats.get(Stats.CritChance);
        cccalc = (int) (Math.random() * 100 + 1);

        double preMultiplier = event.getPreMultiplier();
        double postMult = event.getPostMultiplier();

        double damage;
        if (cccalc <= cc) {
            isCrit = true;
            damage = (5 + (double) weapondmg) * (1 + ((double) stre / 100)) * (1 + ((double) cd / 100)) * ((preMultiplier)) * ((postMult));

        } else {
            damage = (5 + (double) weapondmg) * (1 + ((double) stre / 100)) * ((preMultiplier)) * ((postMult));
        }

        this.damage = damage;
        return damage;
    }

    public void damagePlayer(GamePlayer player) {
        damagePlayer(player, EntityDamageEvent.DamageCause.CUSTOM);
    }

    public void damagePlayer(GamePlayer player, EntityDamageEvent.DamageCause cause) {
        if (projectile == null) result = new GameDamageEvent(player, e, this, type, cause);

        else result = new GameDamageEvent(player, e, this, type, cause, projectile);
        Bukkit.getPluginManager().callEvent(result);
        if (result.isCancelled()) {
            return;}
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) damage = 0;
        if (damage > 0){
            player.damage(0.0001);
        };
        if (MMOStats.absorbtion.get(player.getPlayer()) - damage < 0) {
            float restdamage = (float) damage - (float) MMOStats.absorbtion.get(player);
            MMOStats.absorbtion.replace(player, 0);
            player.setHealth(player.currhealth - (int) restdamage, HealthChangeReason.Damage);
        } else {
            MMOStats.absorbtion.replace(player, MMOStats.absorbtion.get(player) - (int) damage);
        }
        MMOStats.updatebar(player);
    }

    public void damageEntity(LivingEntity e, GamePlayer player) {
        
        damageEntity(e, player, EntityDamageEvent.DamageCause.CUSTOM);
    }

    public void damageEntity(LivingEntity e, GamePlayer player, EntityDamageEvent.DamageCause cause) {
        
        if (MMOStats.entitydead.containsKey(e)) return;
        if (e.getScoreboardTags().contains("npc")) return;
        if (EntityMap.exists(e)) {
            EntityAtributes atributes = EntityMap.getGmEntity(e).getClass().getAnnotation(EntityAtributes.class);
            if (atributes != null) for (EntityAtributes.Attributes attributes : atributes.value()) {
                if (attributes == EntityAtributes.Attributes.MeleeImunity) return;
                if (attributes == EntityAtributes.Attributes.ProjectileImune && projectile != null) return;
            }
        }
        if (projectile == null) result = new GameDamageEvent(player, e, this, type, cause);
        else result = new GameDamageEvent(player, e, this, type, cause, projectile);
        Bukkit.getPluginManager().callEvent(result);

        isCanceled = result.isCancelled();
        if (result.isCancelled()) return;


        if (e.getScoreboardTags().contains("invinc")) {
            return;
        }
        if (!ignoreHit && e != null)
            EntityHandler.set("hit", e, PersistentDataType.INTEGER, EntityHandler.getOrDefault("hit", e, PersistentDataType.INTEGER, 0) + 1);
        int newHealth;
        if (!EntityMap.exists(e)) new BasicEntity(e);
        GameEntity se = EntityMap.getGmEntity(e);
        if (GameEntity.isExtention(e)) se = GameEntity.getExtention(e).owner();
        if (se instanceof FinalDamageDesider desider) damage = desider.getFinalDamage(player, damage);
        se.damage(damage, GamePlayer.getGamePlayer(player));
        newHealth = se.getHealth();

        e.damage(0.00001, player);
        if (EntityMap.exists(e) && EntityMap.getGmEntity(e).hasNoKB())
            e.setVelocity(new Vector(0, 0, 0));

        if (type == GameDamageEvent.DamageType.PlayerToEntity) {
            GameDamagePlayerToEntityExecuteEvent event = new GameDamagePlayerToEntityExecuteEvent(player, e, this);
            Bukkit.getPluginManager().callEvent(event);
        }


        if (newHealth <= 0) {
            if (player != null) e.addScoreboardTag("killer:" + player.getName());
        } else MMOStats.updateentitystats(e);

        if ((EntityMap.exists(e) && EntityMap.getGmEntity(e).getHealth() <= 0)) {
            GameEntity.killEntity(se, player);
            if (player != null) if (projectile != null) {
                e.addScoreboardTag("arrowkill:" + player.getName());
            }
            if (isMagic) e.addScoreboardTag("abilitykill");
            MMOStats.updateentitystats(e);
        } else {
            if (EntityMap.exists(e) && element != null) {
                Elementable.addElement(EntityMap.getGmEntity(e), element);
            }
        }
    }

    public void showDamageTag(Entity e) {
        Location loc = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + 0.7, e.getLocation().getZ());
        showDamageTag(loc, e);
    }

    public void showDamageTag(Location loc, Entity target) {
        if (isCanceled) return;
        if (result != null && result.isCancelled()) return;

        loc = loc.clone().add(new Random().nextDouble(0.4) - 0.2, new Random().nextDouble(0.4) - 0.2, new Random().nextDouble(0.4) - 0.2);
        final String str = String.format("%.0f", (Tools.round(damage, 0)));

        // Criar o ArmorStand com configurações básicas
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, armorstand -> {
            armorstand.setVisible(false);
            armorstand.setGravity(false);
            armorstand.setMarker(true);
            armorstand.setCustomNameVisible(true);
            armorstand.setInvulnerable(true);

            if (isCrit) {
                TextComponent name = Component.text("✧").color(NamedTextColor.GRAY);
                String num = "" + str;
                int col = 1;
                int coltype = 1;
                NamedTextColor colstr = NamedTextColor.WHITE;

                for (char x : num.toCharArray()) {
                    name = name.append(Component.text(x).color(colstr));
                    ++col;
                    if (col == 2) {
                        col = 0;
                        ++coltype;
                        switch (coltype) {
                            case 1 -> colstr = NamedTextColor.WHITE;
                            case 2 -> colstr = NamedTextColor.YELLOW;
                            case 3 -> {
                                colstr = NamedTextColor.GOLD;
                                coltype = 0;
                            }
                        }

                    }
                }
                String x = "✧";
                name = name.append(Component.text(x).color(colstr));
                armorstand.customName(name);
            } else armorstand.customName(Component.text(str).color(NamedTextColor.GRAY));

            armorstand.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 999999));
            armorstand.addScoreboardTag("damage_tag");
            armorstand.setArms(false);
            armorstand.setBasePlate(false);
        });

        // Se o alvo for um jogador, usamos ProtocolLib para modificar o pacote de metadata
        if (target instanceof Player targetPlayer) {
            try {
                ProtocolManager protocolManager = Hook.getProtocolManager();
                if (protocolManager != null) {
                    // Registrar um interceptador de pacotes para este jogador específico
                    protocolManager.addPacketListener(
                            new PacketAdapter(MMOStats.getInstance(), ListenerPriority.NORMAL,
                                    PacketType.Play.Server.ENTITY_METADATA) {
                                @Override
                                public void onPacketSending(PacketEvent event) {
                                    // Verificar se o pacote é para o jogador alvo
                                    if (event.getPlayer().equals(targetPlayer)) {
                                        // Verificar se o pacote é para o ArmorStand específico
                                        if (event.getPacket().getIntegers().read(0) == stand.getEntityId()) {
                                            // Cancelar o envio deste pacote de metadata
                                            event.setCancelled(true);

                                            // Remover o listener após interceptar o pacote
                                            protocolManager.removePacketListener(this);
                                        }
                                    }
                                }
                            }
                    );
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Erro ao configurar interceptador de pacotes: " + e.getMessage());
            }
        }

        // Remover o ArmorStand após um tempo
        MMOStats.getInstance().killarmorstand(stand);
    }


    public void showFireDamageTag(Entity e) {
        
        Location loc = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + 0.7, e.getLocation().getZ());
        final String str = String.format("%.0f", (Tools.round(damage, 0)));
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, armorstand -> {
            armorstand.setVisible(false);
            armorstand.setGravity(false);
            armorstand.setMarker(true);
            armorstand.setCustomNameVisible(true);
            armorstand.setInvulnerable(true);
            armorstand.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 999999));
            armorstand.addScoreboardTag("damage_tag");
            armorstand.setArms(false);
            armorstand.setBasePlate(false);
            armorstand.customName(Component.text(str).color(NamedTextColor.GOLD));
        });
        MMOStats.getInstance().killarmorstand(stand);
    }

    public void showThunderDamageTag(Entity e) {
        
        Location loc = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + 0.7, e.getLocation().getZ());
        final String str = String.format("%.0f", (Tools.round(damage, 0)));
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, armorstand -> {
            armorstand.setVisible(false);
            armorstand.setGravity(false);
            armorstand.setMarker(true);
            armorstand.setCustomNameVisible(true);
            armorstand.setInvulnerable(true);
            armorstand.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 999999));
            armorstand.addScoreboardTag("damage_tag");
            armorstand.setArms(false);
            armorstand.setBasePlate(false);
            armorstand.setCustomName("§9" + str);
        });
        MMOStats.getInstance().killarmorstand(stand);
    }

    public void playerToEntityMagicDamage(GamePlayer player, @Nullable LivingEntity e, double magicDamage) {
        
        this.e = e;
        if (e != null && e.getScoreboardTags().contains("npc")) return;

        if (e != null && e.getScoreboardTags().contains("abilityimun")) return;
        if (e != null && EntityMap.exists(e)) {
            EntityAtributes atributes = EntityMap.getGmEntity(e).getClass().getAnnotation(EntityAtributes.class);
            if (atributes != null) for (EntityAtributes.Attributes attributes : atributes.value())
                if (attributes == EntityAtributes.Attributes.AbilityImune) return;
        }
        double abilityDamage = 0, inteligens = 0;
        if (player != null) {
            abilityDamage = MMOStats.getPlayerStat(player, Stats.AbilityDamage);
            inteligens = MMOStats.getPlayerStat(player, Stats.Inteligence);
        }

        double baseMult = 0;
        DamagePrepairEvent event = new DamagePrepairEvent(player, e, this, 1d, 1d, getIfMissing(new HashMap<>(), player), MMOStats.getPlayerStat(player, Stats.WeaponDamage));
        Bukkit.getPluginManager().callEvent(event);

        damage = magicDamage * (1 + (inteligens / 100) * abilityScaling) * (1 + (baseMult / 100)) * (1 + (abilityDamage / 100)) * ((event.getPreMultiplier())) * ((event.getPostMultiplier()));
        if (e != null && GameEntity.livingEntity.exists(e) && GameEntity.livingEntity.getGmEntity(e).getClass().getAnnotation(EntityAtributes.MagicResistance.class) != null) {
            GameEntity entity = GameEntity.livingEntity.getGmEntity(e);
            EntityAtributes.MagicResistance resistance = entity.getClass().getAnnotation(EntityAtributes.MagicResistance.class);
            damage *= resistance.multiplier();
        }
    }

    public void setMagic(String str) {
        isMagic = true;
        abilityName = str;
    }

    public void setMagic(String str, double abilityScaling) {
        isMagic = true;
        abilityName = str;
        this.abilityScaling = abilityScaling;
    }

    public void sendMagicMessage(int entityAmount, GamePlayer player) {
        if (!isMagic) throw new CalculatorException("There is no magic");
        player.sendMessage("§7Your " + abilityName + " hit §c" + entityAmount + "§7 enemy" + ((entityAmount > 1) ? "s" : "") + " for §c" + String.format("%.0f", damage) + " §7damage.");
    }

    public boolean isMagic() {
        return isMagic;
    }

    private int getGameEntityHealth(LivingEntity e) {
        return GameEntity.livingEntity.getGmEntity(e).getHealth();
    }
}
