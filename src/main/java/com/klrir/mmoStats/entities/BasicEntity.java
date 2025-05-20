package com.klrir.mmoStats.entities;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.elements.Element;
import com.klrir.mmoStats.game.EntityMap;
import com.klrir.mmoStats.game.GameEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Set;

public class BasicEntity extends GameEntity {
    private static final HashMap<EntityType, LootTable> lootTables = new HashMap<>();
    private LivingEntity entity;
    private final int maxHealth;
    private final int damage;
    private Class<? extends LivingEntity> aClass;
    private LootTable lootTable;
    public BasicEntity(LivingEntity entity) {
        this(entity, (int) (entity.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * 5));
    }
    public BasicEntity(LivingEntity entity, int maxHealth) {
        super();
        if (entity instanceof ArmorStand) throw new IllegalArgumentException("Not allowed to use an armorstand!");
        health = maxHealth;
        this.entity = entity;
        this.maxHealth = maxHealth;
        AttributeInstance atr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        damage = (atr == null) ? 0 : (int) (atr.getBaseValue() * 5);
        EntityMap.addEntity(entity, this);
        MMOStats.updateentitystats(entity);
    }

    @Override
    public void addElement(Element element) {

    }

    @Override
    public Set<Element> getElements() {
        return null;
    }

    @Override
    public void removeElement(Element element) {

    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getName() {
        return entity.getType().getName();
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }
}
