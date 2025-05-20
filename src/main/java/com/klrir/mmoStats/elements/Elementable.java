package com.klrir.mmoStats.elements;

import com.klrir.mmoStats.game.GameEntity;

import java.util.Set;

public interface Elementable {
    void addElement(Element element);
    Set<Element> getElements();
    void removeElement(Element element);
    static void addElement(GameEntity entity, Element element){
        entity.addElement(element);
    }
}
