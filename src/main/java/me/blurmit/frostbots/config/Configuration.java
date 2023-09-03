package me.blurmit.frostbots.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Configuration {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configurations;

    @ConfigEntry
    @Getter
    private List<String> allowedTypes;
    @ConfigEntry
    @Getter
    private int botRequestThreshold;
    @ConfigEntry
    @Getter
    private String botSkin;
    @ConfigEntry
    @Getter
    private String botName;
    @ConfigEntry
    @Getter
    private Location spawnLocation;
    @ConfigEntry(configType = "messages.yml")
    @Getter
    private List<String> botDuelReceived;

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configurations = new HashMap<>();

        load();
    }

    private void load() {
        File frostDirectory = new File("./plugins/Frost/");

        if (frostDirectory.isDirectory()) {
            for (File f : frostDirectory.listFiles()) {
                create(f.getName());
            }
        }

        for (Field field : Configuration.class.getDeclaredFields()) {
            field.setAccessible(true);
            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);

            if (configEntry == null) {
                continue;
            }

            String file = configEntry.configType();
            FileConfiguration config = get(file);
            String name = field.getName().equals("spawnLocation") ? field.getName() : parseFieldName(field.getName());
            Object value = config.get(name);

            if (value != null) {
                if (name.equals("spawnLocation")) {
                    spawnLocation = getLocation(config, field.getName());
                    continue;
                }

                try {
                    field.set(this, value);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public FileConfiguration getDefault() {
        return get("config.yml");
    }

    public FileConfiguration get(String name) {
        return configurations.get(name);
    }

    public void create(String name) {
        File file = new File("./plugins/Frost/" + name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred whilst attempting to create a new configuration file.", e);
            }
        }

        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
        configurations.put(name, newConfig);
    }

    public Location getLocation(FileConfiguration config, String path) {
        String rawLocation = config.getString(path);
        String[] locationParts = rawLocation.split(", ");
        String rawX = locationParts[0];
        String rawY = locationParts[1];
        String rawZ = locationParts[2];

        try {
            double x = Double.parseDouble(rawX);
            double y = Double.parseDouble(rawY);
            double z = Double.parseDouble(rawZ);
            float yaw = 0;
            float pitch = 0;
            String worldName = "world";

            if (locationParts.length == 4) {
                worldName = locationParts[3];
            }

            if (locationParts.length == 5) {
                yaw = Float.parseFloat(locationParts[3]);
                pitch = Float.parseFloat(locationParts[4]);
            }

            if (locationParts.length == 6) {
                worldName = locationParts[5];
                yaw = Float.parseFloat(locationParts[3]);
                pitch = Float.parseFloat(locationParts[4]);
            }

            World world = Bukkit.getWorld(worldName);
            Location location = new Location(world, x, y, z, yaw, pitch);

            if (world == null) {
                plugin.getLogger().severe("Failed to create a location instance. The " + worldName + " world does not exist.");
                String finalWorldName = worldName;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> location.setWorld(Bukkit.getWorld(finalWorldName)), 100L);
            }

            return location;
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to serialize location. Was the specified path valid?", e);
            return null;
        }
    }

    public List<PotionEffect> getPotionEffects(FileConfiguration config, String path) {
        String input = config.getString(path);

        if (input.isEmpty()) {
            return new ArrayList<>();
        }

        List<PotionEffect> potionEffects = new ArrayList<>();
        String[] effectStrings = input.split(";");

        for (String effectString : effectStrings) {
            String[] parts = effectString.split(":");

            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid potion effect format: " + input);
            }

            String typeString = parts[0].substring(2);  // Remove the 'n@' prefix
            String durationString = parts[1].substring(2);  // Remove the 'd@' prefix
            String amplifierString = parts[2].substring(2);  // Remove the 'a@' prefix

            PotionEffectType type = PotionEffectType.getByName(typeString.toUpperCase());

            if (type == null) {
                throw new IllegalArgumentException("Invalid potion effect type: " + typeString);
            }

            int duration = Integer.parseInt(durationString);
            int amplifier = Integer.parseInt(amplifierString);

            potionEffects.add(new PotionEffect(type, duration, amplifier));
        }

        return potionEffects;
    }

    private String parseFieldName(String name) {
        StringBuilder builder = new StringBuilder(name);
        int length = name.length();

        if (length > 0) {
            builder.replace(0, 1, Character.toUpperCase(name.charAt(0)) + "");
        }

        int inserted = 0;
        for (int i = 1; i < length; i++) {
            char letter = name.charAt(i);

            if (!Character.isUpperCase(letter)) {
                continue;
            }

            builder.insert(i + inserted++, '-');
        }

        return builder.toString();
    }

}
