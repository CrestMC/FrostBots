package me.blurmit.frostbots.bot;

import dev.imanity.knockback.api.Knockback;
import dev.imanity.knockback.api.KnockbackEntity;
import dev.imanity.knockback.api.KnockbackService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class BotKnockbackEntity implements KnockbackEntity {

    private final Entity bot;

    public BotKnockbackEntity(Entity bot) {
        this.bot = bot;
    }

    @Override
    public double getX() {
        return bot.getLocation().getX();
    }

    @Override
    public double getY() {
        return bot.getLocation().getY();
    }

    @Override
    public double getZ() {
        return bot.getLocation().getZ();
    }

    @Override
    public float yaw() {
        return bot.getLocation().getYaw();
    }

    @Override
    public float pitch() {
        return bot.getLocation().getPitch();
    }

    @Override
    public double motX() {
        return bot.getVelocity().getX();
    }

    @Override
    public double motY() {
        return bot.getVelocity().getY();
    }

    @Override
    public double motZ() {
        return bot.getVelocity().getZ();
    }

    @Override
    public Random random() {
        return new Random();
    }

    @Override
    public void setAI(boolean b) {}

    @Override
    public void addMotX(double v) {
        Vector vector = bot.getVelocity();
        vector.setX(vector.getX() + v);

        bot.setVelocity(vector);
    }

    @Override
    public void addMotY(double v) {
        Vector vector = bot.getVelocity();
        vector.setY(vector.getY() + v);

        bot.setVelocity(vector);
    }

    @Override
    public void addMotZ(double v) {
        Vector vector = bot.getVelocity();
        vector.setZ(vector.getZ() + v);

        bot.setVelocity(vector);
    }

    @Override
    public void removeMotX(double v) {
        Vector vector = bot.getVelocity();
        vector.setX(vector.getX() - v);

        bot.setVelocity(vector);
    }

    @Override
    public void removeMotY(double v) {
        Vector vector = bot.getVelocity();
        vector.setY(vector.getY() - v);

        bot.setVelocity(vector);
    }

    @Override
    public void removeMotZ(double v) {
        Vector vector = bot.getVelocity();
        vector.setZ(vector.getZ() - v);

        bot.setVelocity(vector);
    }

    @Override
    public void setMotX(double v) {
        Vector vector = bot.getVelocity();
        vector.setX(v);

        bot.setVelocity(vector);
    }

    @Override
    public void setMotY(double v) {
        Vector vector = bot.getVelocity();
        vector.setY(v);

        bot.setVelocity(vector);
    }

    @Override
    public void setMotZ(double v) {
        Vector vector = bot.getVelocity();
        vector.setZ(v);

        bot.setVelocity(vector);
    }

    @Override
    public void multiplyMotX(double v) {
        Vector vector = bot.getVelocity();
        vector.setX(vector.getX() * v);

        bot.setVelocity(vector);
    }

    @Override
    public void multiplyMotY(double v) {
        Vector vector = bot.getVelocity();
        vector.setY(vector.getY() * v);

        bot.setVelocity(vector);
    }

    @Override
    public void multiplyMotZ(double v) {
        Vector vector = bot.getVelocity();
        vector.setZ(vector.getZ() * v);

        bot.setVelocity(vector);
    }

    @Override
    public void divMotX(double v) {
        Vector vector = bot.getVelocity();
        vector.setX(vector.getX() / v);

        bot.setVelocity(vector);
    }

    @Override
    public void divMotY(double v) {
        Vector vector = bot.getVelocity();
        vector.setY(vector.getY() / v);

        bot.setVelocity(vector);
    }

    @Override
    public void divMotZ(double v) {
        Vector vector = bot.getVelocity();
        vector.setZ(vector.getZ() / v);

        bot.setVelocity(vector);
    }

    @Override
    public void setSprinting(boolean b) {
        if (bot instanceof Player) {
            ((Player) bot).setSprinting(b);
        }
    }

    @Override
    public boolean isSprinting() {
        if (bot instanceof Player) {
            return ((Player) bot).isSprinting();
        }

        return false;
    }

    @Override
    public boolean isOnGround() {
        return bot.isOnGround();
    }

    @Override
    public void addMotion(double v, double v1, double v2) {
        bot.getVelocity().add(new Vector(v, v1, v2));
    }

    @Override
    public void setMotion(double v, double v1, double v2) {
        bot.setVelocity(new Vector(v, v1, v2));
    }

    @Override
    public void sendVelocityPacketImmediately(double v, double v1, double v2) {
        setMotion(v, v1, v2);
    }

    @Override
    public int ping() {
        return 0;
    }

    @Override
    public Collection<PotionEffect> potions() {
        if (bot instanceof Player) {
            return ((Player) bot).getActivePotionEffects();
        }

        return Collections.emptyList();
    }

    @Override
    public Entity getBukkitEntity() {
        return bot;
    }

    @Override
    public Knockback getKnockback() {
        Knockback globalKnockback = KnockbackService.get().getGlobalKnockback();
        Knockback knockback = KnockbackService.get().getKnockback((Player) bot);
        return knockback != null ? knockback : globalKnockback;
    }

    @Override
    public <T> T getValue(String s, Class<T> aClass, T t) {
        return null;
    }

    @Override
    public <T> void setValue(String s, T t) {

    }
}
