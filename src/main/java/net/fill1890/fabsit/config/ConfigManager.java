package net.fill1890.fabsit.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.error.LoadConfigException;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * Config manager
 */
public abstract class ConfigManager {
    // GSON config
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
    // config data
    private static Config CONFIG;
    // language data
    public static Map<String, String> LANG;
    // blocks currently occupied for posing
    public static ArrayList<BlockPos> occupiedBlocks = new ArrayList<>();

    // players that have the mod loaded
    public static final ArrayList<SocketAddress> loadedPlayers = new ArrayList<>();

    public static Config getConfig() {
        return CONFIG;
    }

    public static void loadConfig() throws LoadConfigException {
        CONFIG = null;

        Config config;
        String json = "";
        BufferedWriter writer;

        // try to load the config file
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "fabsit.json");

        if(configFile.exists()) {
            // exists: read into config object
            try {
                json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            } catch(FileNotFoundException ignored) {} // should never occur - previous check
              catch(IOException e) {
                FabSit.LOGGER.error("I/O error reading FabSit config file!");
                throw new LoadConfigException();
            }

            try {
                config = GSON.fromJson(json, Config.class);
            } catch(JsonSyntaxException e) {
                FabSit.LOGGER.error("Invalid JSON structure in config file!");
                throw new LoadConfigException();
            }

        } else {
            // doesn't exist: use defaults
            config = new Config();
        }

        // write the config back to the file
        // creates the file on first run and purges invalid properties
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
        } catch(FileNotFoundException e) {
            FabSit.LOGGER.error("Error writing to config file! Is the file writable?");
            throw new LoadConfigException();
        }

        try {
            writer.write(GSON.toJson(config));
            writer.close();
        } catch(IOException e) {
            FabSit.LOGGER.error("Error writing to config file!");
            throw new LoadConfigException();
        }

        // load localizations for server translation
        LANG = loadLocalizations(config.locale);
        CONFIG = config;
    }

    private static Map<String, String> loadLocalizations(@NotNull String locale) {
        InputStream localeFile;
        String json;

        // try to load the config file
        localeFile = ConfigManager.class.getClassLoader().getResourceAsStream("assets/fabsit/lang/" + locale + ".json");

        if(localeFile == null && !locale.equals("en_us")) {
            // not found - default to en_us
            FabSit.LOGGER.warn("FabSit locale " + locale + " not found! Attempting to fall back to en_us...");
            return loadLocalizations("en_us");
        } else if(localeFile == null) {
            // en_us not found
            FabSit.LOGGER.error("FabSit localizations not found! Translations will not be complete");
            return null;
        }

        // read the file in
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

        // typedef for GSON read to force Map<String, String>
        Type emptyMapType = new TypeToken<Map<String, String>>() {}.getType();

        // load data into lang map
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
