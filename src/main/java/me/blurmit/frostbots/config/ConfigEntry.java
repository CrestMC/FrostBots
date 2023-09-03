package me.blurmit.frostbots.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigEntry {

    String configType() default "config.yml";

}
