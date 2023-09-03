package me.blurmit.frostbots.util;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MaterialUtil {

    public static boolean isHelmet(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getType().isBlock()) {
            return false;
        }

        return itemStack.getType().name().endsWith("_HELMET");
    }

    public static boolean hasHelmet(List<ItemStack> items) {
        return items.stream().anyMatch(MaterialUtil::isHelmet);
    }

    public static boolean isChestplate(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getType().isBlock()) {
            return false;
        }

        return itemStack.getType().name().endsWith("_CHESTPLATE");
    }

    public static boolean hasChestplate(List<ItemStack> items) {
        return items.stream().anyMatch(MaterialUtil::isChestplate);
    }

    public static boolean isLeggings(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getType().isBlock()) {
            return false;
        }

        return itemStack.getType().name().endsWith("_LEGGINGS");
    }

    public static boolean hasLeggings(List<ItemStack> items) {
        return items.stream().anyMatch(MaterialUtil::isLeggings);
    }

    public static boolean isBoots(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getType().isBlock()) {
            return false;
        }

        return itemStack.getType().name().endsWith("_BOOTS");
    }

    public static boolean hasBoots(List<ItemStack> items) {
        return items.stream().anyMatch(MaterialUtil::isBoots);
    }

    public static boolean isArmor(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (itemStack.getType().isBlock()) {
            return false;
        }

        return isHelmet(itemStack) || isChestplate(itemStack) || isLeggings(itemStack) || isBoots(itemStack);
    }

    public static double getDamage(ItemStack item) {
        switch (item.getType().toString()) {
            case "WOOD_SWORD":
            case "GOLD_SWORD":
            case "DIAMOND_SPADE":
            case "IRON_PICKAXE":
            case "STONE_AXE":
                return 4.0D;
            case "STONE_SWORD":
            case "DIAMOND_PICKAXE":
            case "IRON_AXE":
                return 5.0D;
            case "IRON_SWORD":
            case "DIAMOND_AXE":
                return 6.0D;
            case "DIAMOND_SWORD":
                return 7.0D;
            case "STONE_SPADE":
            case "WOOD_PICKAXE":
            case "GOLD_PICKAXE":
                return 2.0D;
            case "IRON_SPADE":
            case "STONE_PICKAXE":
            case "WOOD_AXE":
            case "GOLD_AXE":
                return 3.0D;
            default:
                return 1.0D;
        }
    }

}
