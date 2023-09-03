package me.blurmit.frostbots.kit;

import java.util.HashMap;
import java.util.Map;

public enum KnockbackProfile {

    STRAFE,
    SUMO,
    BOXING,
    WTAP,
    COMBO,
    DEFAULT,
    BED,
    BUILD;

    private static final Map<String, KnockbackProfile> BY_NAME = new HashMap<>();

    public String getName() {
        return name().toLowerCase();
    }

    public static KnockbackProfile getByName(String name) {
        return BY_NAME.getOrDefault(name, KnockbackProfile.DEFAULT);
    }

    static {
        for (KnockbackProfile value : values()) {
            BY_NAME.put(value.name().toLowerCase(), value);
        }
    }

}
