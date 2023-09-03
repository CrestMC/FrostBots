package me.blurmit.frostbots.arena;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@Data
@NoArgsConstructor
public class Arena {

    private String name;
    private boolean enabled;
    private Location spawnPositionA;
    private Location spawnPositionB;
    private Location min;
    private Location max;
    private int maxBuildHeight;
    private int deadZone;

}
