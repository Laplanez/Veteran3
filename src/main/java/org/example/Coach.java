package org.example;

public class Coach extends Participant {
    public Coach(String name, int id) {
        super(name, id);
    }

    public void train(Player player, String attribute) {
        int current = player.getAttribute(attribute);
        player.setAttribute(attribute, current + 1);
    }
}


class Tactic {
    private final String name;
    private final double attackModifier;
    private final double defenseModifier;

    public Tactic(String name, double attackModifier, double defenseModifier) {
        this.name = name;
        this.attackModifier = attackModifier;
        this.defenseModifier = defenseModifier;
    }

    public String getName() { return name; }
    public double getAttackModifier() { return attackModifier; }
    public double getDefenseModifier() { return defenseModifier; }

    public static Tactic createAttack() {
        return new Tactic("Attack", 1.2, 0.8);
    }

    public static Tactic createBalanced() {
        return new Tactic("Balanced", 1.0, 1.0);
    }

    public static Tactic createDefense() {
        return new Tactic("Defense", 0.8, 1.2);
    }
}