package net.fill1890.fabsit.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.netty.util.Attribute;
import net.fabricmc.loader.api.FabricLoader;
import net.fill1890.fabsit.FabSit;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * Config manager
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
    private static Config CONFIG;
    public static Map<String, String> LANG;

    public static ArrayList<ServerPlayerEntity> loadedPlayers = new ArrayList<>();

    public static Config getConfig() {
        return CONFIG;
    }

    public static boolean loadConfig() {
        CONFIG = null;

        Config config;
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "fabsit.json");
        String json = "";
        BufferedWriter writer;

        if(configFile.exists()) {
            try {
                json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            } catch(FileNotFoundException ignored) {} // should never occur - previous check
              catch(IOException e) {
                FabSit.LOGGER.error("I/O error reading FabSit config file!");
                return false;
            }

            try {
                config = GSON.fromJson(json, Config.class);
            } catch(JsonSyntaxException e) {
                FabSit.LOGGER.error("Invalid JSON structure in config file!");
                return false;
            }
        } else {
            config = new Config();
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        } catch(FileNotFoundException e) {
            FabSit.LOGGER.error("Error writing to config file! Is the file writable?");
            return false;
        }

        try {
            writer.write(GSON.toJson(config));
            writer.close();
        } catch(IOException e) {
            FabSit.LOGGER.error("Error writing to config file!");
            return false;
        }

        LANG = loadLocalizations(config.locale);

        CONFIG = config;
        return true;
    }

    private static Map<String, String> loadLocalizations(@NotNull String locale) {
        InputStream localeFile;
        String json = "";

        localeFile = ConfigManager.class.getClassLoader().getResourceAsStream("assets/fabsit/lang/" + locale + ".json");

        if(localeFile == null && !locale.equals("en_us")) {
            FabSit.LOGGER.warn("FabSit locale " + locale + " not found! Attempting to fall back to en_us...");
            return loadLocalizations("en_us");
        } else if(localeFile == null) {
            FabSit.LOGGER.error("FabSit localizations not found! Translations will not be complete");
            return null;
        }

        try {
            json = IOUtils.toString(new BufferedReader(new InputStreamReader(localeFile)));
        } catch(IOException e) {
            if(!locale.equals("en_us")) {
                FabSit.LOGGER.warn("Error loading FabSit locale " + locale + "! Attempting to fall back to en_us...");
                return loadLocalizations("en_us");
            } else {
                FabSit.LOGGER.error("Error loading FabSit locales! Translations will not be done");
                return null;
            }
        }

        Type emptyMapType = new TypeToken<Map<String, String>>() {}.getType();

        try {
            return GSON.fromJson(json, emptyMapType);
        } catch(JsonParseException e) {
            if(!locale.equals("en_us")) {
                FabSit.LOGGER.warn("FabSit locale " + locale + " is not valid! Attempting to fall back to en_us...");
                return loadLocalizations("en_us");
            } else {
                FabSit.LOGGER.error("Error loading FabSit locales - no valid locale file! Translations will not be done");
                return null;
            }
        }
    }
}
