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
    public Tactic(String name) { this.name = name; }
    public String getName() { return name; }
}