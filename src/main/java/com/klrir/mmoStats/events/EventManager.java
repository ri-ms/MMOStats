package com.klrir.mmoStats.events;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.database.Database;
import com.klrir.mmoStats.entities.BasicEntity;
import com.klrir.mmoStats.entities.StandCoreExtention;
import com.klrir.mmoStats.game.*;
import fr.skytasul.glowingentities.GlowingEntities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EventManager implements Listener {
    public EventManager Events;

    @EventHandler
    public void onJoin(PlayerJoinEvent event){

        MMOStats.absorbtion.put(event.getPlayer(), 0);
        MMOStats.absorbtionrunntime.put(event.getPlayer(), 0);

        GamePlayer player = new GamePlayer((CraftServer) MMOStats.getInstance().getServer(), ((CraftPlayer) event.getPlayer()).getHandle());

        GlowingEntities glowingEntities = MMOStats.getGlowingEntities();

        Bukkit.getScheduler().runTaskLaterAsynchronously(MMOStats.getInstance(), () -> {
            for (Player gp : Bukkit.getOnlinePlayers()) {
                try {
                    glowingEntities.setGlowing(gp, player, ChatColor.DARK_PURPLE);
                    glowingEntities.setGlowing(player, gp, ChatColor.DARK_PURPLE);
                } catch (ReflectiveOperationException e) {
                    MMOStats.LOGGER.info("Erro ao enviar o GlowingEffect de " + gp.getName() + " para " + player.getName());
                }
            }
        }, 20);

    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        GamePlayer player = GamePlayer.getGamePlayer(event.getPlayer());

        player.setHealth(MMOStats.getPlayerStat(player, Stats.Health));

        MMOStats.deathPersons.remove(event.getPlayer());

        MMOStats.updatebar(player);
    }

    @EventHandler
    public void SoulSystem(EntityDeathEvent e) {
        if (e.getEntityType() != EntityType.PLAYER && e.getEntityType() != EntityType.ITEM) {

        } else {
            if (e.getEntity() instanceof Player p) {
                GamePlayer player = GamePlayer.getGamePlayer(p);
                if (Slayer.getSlayer(player) != null) {
                    GameEntity.killEntity(Slayer.getSlayer(player), null);
                }
            }
        }
    }

    @EventHandler
    public void DamageEvent(org.bukkit.event.entity.EntityDamageEvent event) {
        if (MMOStats.entitydead.containsKey(event.getEntity())) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        if (event.getEntity() instanceof Player) {
            GamePlayer player = GamePlayer.getGamePlayer((Player) event.getEntity());

            if (MMOStats.getInstance().getConfig().getBoolean("StatSystem")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setDamage(0);
                    int falldistance = (int) player.getFallDistance();

                    int damage = (int) (falldistance * 5) - 15;
                    if (damage < 0) damage = 0;

                    if (MMOStats.absorbtion.get(player) - damage < 0) {
                        int restdamage = damage - MMOStats.absorbtion.get(player);
                        MMOStats.absorbtion.replace(player, 0);
                        if (player.currhealth - restdamage < 0) restdamage = player.currhealth;
                        player.setHealth(player.currhealth - restdamage, HealthChangeReason.Damage);
                    } else {
                        MMOStats.absorbtion.replace(player, MMOStats.absorbtion.get(player) - damage);
                    }

                    if (player.currhealth <= 0) {
                        player.getPlayer().setHealth(0);
                    } else MMOStats.updatebar(player);
                }
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) player.setHealth(0, HealthChangeReason.Force);
                MMOStats.updatebar(player);
            }
        } else {
            if (event.getEntityType() != EntityType.ARMOR_STAND && !(event.getEntity().getType() == EntityType.WITHER_SKULL)) {
                if (event.getEntity() instanceof LivingEntity) {
                    if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                        LivingEntity e = (LivingEntity) event.getEntity();
                        int damage = (int) event.getDamage();
                        event.setDamage(0D);
                    } else {
                        MMOStats.updateentitystats((LivingEntity) event.getEntity());
                    }
                } else {
                    event.setDamage(0);
                }
            }
        }
    }

    @EventHandler
    public void EntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamage() <= 0.1) {
            return;
        }

        if (GameEntity.isOnCooldown((LivingEntity) event.getDamager())) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)
            GameEntity.setOnCooldown((LivingEntity) event.getDamager());

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            GamePlayer player = GamePlayer.getGamePlayer((Player) event.getDamager());
            GamePlayer target = GamePlayer.getGamePlayer((Player) event.getEntity());
            float damage = (float) (event.getDamage() * 8);
            event.setDamage(0);
            event.setCancelled(true);
            Calculator c = new Calculator();

            c.playerToPlayerDamage(target, player);
            c.damagePlayer(target);
            c.showDamageTag(target);
            return;
        }

        Entity damager = event.getDamager();

        if (event.getEntity() instanceof Player && damager instanceof LivingEntity) {
            GamePlayer player = GamePlayer.getGamePlayer((Player) event.getEntity());

            if (MMOStats.getInstance().getConfig().getBoolean("StatSystem")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);

                    int falldistance = (int) player.getFallDistance();

                    int damage = (int) (falldistance * 5) - 15;
                    if (damage < 0) damage = 0;
                    if (MMOStats.absorbtion.get(player) - damage < 0) {
                        int restdamage = damage - MMOStats.absorbtion.get(player);
                        MMOStats.absorbtion.replace(player, 0);
                        player.setHealth(player.currhealth - restdamage, HealthChangeReason.Damage);
                    } else {
                        MMOStats.absorbtion.replace(player, MMOStats.absorbtion.get(player) - damage);
                    }
                    MMOStats.updatebar(player);
                } else {
                    float damage = (float) (event.getDamage() * 8);
                    event.setDamage(0);
                    event.setCancelled(true);
                    Calculator c = new Calculator();
                    if (!EntityMap.exists(event.getDamager()))
                        new BasicEntity((LivingEntity) event.getDamager());
                    c.entityToPlayerDamage(EntityMap.getGmEntity(event.getDamager()), player);
                    c.damagePlayer(player);
                }
            }
        } else {
            //if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            //    event.setCancelled(true);
            //    return;
            //}
            StandCoreExtention sbE = GameEntity.getExtention(event.getEntity());
            if (sbE != null && event.getDamager() instanceof Player p) {
                GamePlayer player = GamePlayer.getGamePlayer(p);
                GameEntity entity = sbE.owner();
                Calculator calculator = new Calculator();
                calculator.playerToEntityDamage(entity.getEntity(), player);
                calculator.damageEntity(entity.getEntity(), player);
                calculator.showDamageTag(entity.getEntity());
                return;
            }

            if (event.getEntityType() != EntityType.ITEM && event.getEntityType() != EntityType.ARMOR_STAND && !(event.getEntity().getType() == EntityType.WITHER_SKULL) && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity e = (LivingEntity) event.getEntity();
                    if (MMOStats.entitydead.containsKey(e)) return;

                    event.setDamage(0D);
                    if (event.getDamager() instanceof Player) {
                        Player player = (Player) event.getDamager();
                        int cc = (int) MMOStats.getPlayerStat(GamePlayer.getGamePlayer(player), Stats.CritChance);

                        double damage;
                        Calculator calculator = new Calculator();
                        calculator.playerToEntityDamage(e, GamePlayer.getGamePlayer(player));
                        int cccalc = calculator.cccalc;
                        damage = calculator.damage;


                        boolean voidgloom = false;


                        if (player.getItemInHand().getType() == Material.BOW) {
                            return;
                        }


                        event.setDamage(0);
                        if (e.getScoreboardTags().contains("invinc")) {
                            event.setCancelled(true);
                            return;
                        }

                        if (e.getScoreboardTags().contains("voidgloomt1") || e.getScoreboardTags().contains("voidgloomt2") || e.getScoreboardTags().contains("voidgloomt3") || e.getScoreboardTags().contains("voidgloomt4")) {
                            voidgloom = true;
                            final Vector vec = new Vector();
                            e.setVelocity(vec);

                            new BukkitRunnable() {
                                public void run() {
                                    e.setVelocity(vec);
                                }
                            }.runTaskLater(MMOStats.getInstance(), 1l);


                        } else {
                            voidgloom = false;
                        }
                        calculator.damageEntity(e, GamePlayer.getGamePlayer(player), EntityDamageEvent.DamageCause.ENTITY_ATTACK);

                        if (EntityMap.exists(e)) {
                            GameEntity se = EntityMap.getGmEntity(e);
                            if (se.hasNoKB()) {
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        e.setVelocity(new Vector(0, 0, 0));
                                    }
                                }.runTaskLater(MMOStats.getInstance(), 1);
                            }
                        }

                        MMOStats.updateentitystats((LivingEntity) event.getEntity());

                        calculator.showDamageTag(e);
                        e.setCustomNameVisible(true);
                    }

                }
            }
            event.setDamage(0);
        }
    }

    @EventHandler
    public void PlayerXpEvent(PlayerExpChangeEvent event) {
        GamePlayer gamePlayer = GamePlayer.getGamePlayer(event.getPlayer());

        int oldLevel = gamePlayer.getLevelFromXp();
        int obtainedLevel = gamePlayer.addXpToPlayer(event.getAmount() * 2.3);
        System.out.println(gamePlayer.getXp());
        if (oldLevel == obtainedLevel) return;
        for (int level = oldLevel; level < obtainedLevel; level++) {
            final Component mainTitle = Component.text("Level-Up!", NamedTextColor.DARK_GREEN);
            final Component subtitle = Component.text(oldLevel + " > " + level + 1, NamedTextColor.GREEN);
            Title title = Title.title(mainTitle, subtitle);
            gamePlayer.showTitle(title);
        }
    }
}
