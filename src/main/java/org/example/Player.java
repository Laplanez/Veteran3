package org.example;

import java.util.HashMap;
import java.util.Map;

public abstract class Player extends Participant {

    protected Map<String, Integer> attributes;
    protected String position;

    public Player(String name, int id) {
        super(name, id);
        this.attributes = new HashMap<>();
        this.position = "";
    }

    public Player(String name, int id, String position) {
        super(name, id);
        this.attributes = new HashMap<>();
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setAttribute(String skillName, int value) {
        this.attributes.put(skillName, value);
    }

    public int getAttribute(String skillName) {
        return this.attributes.getOrDefault(skillName, 0);
    }

    public Map<String, Integer> getAllAttributes() {
        return this.attributes;
    }
}