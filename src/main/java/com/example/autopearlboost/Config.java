package com.example.autopearlboost;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private Config() {}

    public static boolean enabled = true;
    public static boolean autoUse = true;
    public static double predictionStrength = 1.0;
    public static double maxDistance = 32.0;
    public static double preferredDistance = 12.0;
    public static int predictionTicks = 18;
    public static int preferredPredictionTick = 5;
    public static int minimumTicksAfterThrow = 1;
    public static int cooldownTicks = 6;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autopearlboost.properties");

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) { p.load(r); } catch (IOException ignored) {}
        }
        enabled = bool(p, "enabled", enabled);
        autoUse = bool(p, "autoUse", autoUse);
        predictionStrength = dbl(p, "predictionStrength", predictionStrength);
        maxDistance = dbl(p, "maxDistance", maxDistance);
        preferredDistance = dbl(p, "preferredDistance", preferredDistance);
        predictionTicks = integer(p, "predictionTicks", predictionTicks);
        preferredPredictionTick = integer(p, "preferredPredictionTick", preferredPredictionTick);
        minimumTicksAfterThrow = integer(p, "minimumTicksAfterThrow", minimumTicksAfterThrow);
        cooldownTicks = integer(p, "cooldownTicks", cooldownTicks);
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("autoUse", Boolean.toString(autoUse));
        p.setProperty("predictionStrength", Double.toString(predictionStrength));
        p.setProperty("maxDistance", Double.toString(maxDistance));
        p.setProperty("preferredDistance", Double.toString(preferredDistance));
        p.setProperty("predictionTicks", Integer.toString(predictionTicks));
        p.setProperty("preferredPredictionTick", Integer.toString(preferredPredictionTick));
        p.setProperty("minimumTicksAfterThrow", Integer.toString(minimumTicksAfterThrow));
        p.setProperty("cooldownTicks", Integer.toString(cooldownTicks));
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) { p.store(w, "Auto Pearl Boost configuration"); }
        } catch (IOException ignored) {}
    }

    private static boolean bool(Properties p, String k, boolean d) { return Boolean.parseBoolean(p.getProperty(k, Boolean.toString(d))); }
    private static double dbl(Properties p, String k, double d) { try { return Double.parseDouble(p.getProperty(k, Double.toString(d))); } catch (NumberFormatException e) { return d; } }
    private static int integer(Properties p, String k, int d) { try { return Integer.parseInt(p.getProperty(k, Integer.toString(d))); } catch (NumberFormatException e) { return d; } }
}
