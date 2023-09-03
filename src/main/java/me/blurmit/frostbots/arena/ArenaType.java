package me.blurmit.frostbots.arena;

import java.util.HashMap;
import java.util.Map;

public enum ArenaType {

    RANKED("Ranked"),
    UNRANKED("Unranked"),
    PREMIUM("Premium"),
    UNKNOWN("Unknown");

    private static final Map<String, ArenaType> BY_NAME = new HashMap<>();
    private final String name;

    ArenaType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ArenaType getFromName(String name) {
        return BY_NAME.getOrDefault(name, ArenaType.UNKNOWN);
    }

    static {
        for (ArenaType value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

}
