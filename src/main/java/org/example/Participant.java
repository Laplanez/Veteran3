package org.example;

public abstract class Participant {
    protected String name;
    protected int id;

    public Participant(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}