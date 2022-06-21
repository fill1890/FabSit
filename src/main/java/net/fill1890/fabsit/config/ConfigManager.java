package net.fill1890.fabsit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.fill1890.fabsit.FabSit;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Config manager
 * <br>
 * Implementation based on Patbox's <a href="https://github.com/Patbox/StyledChat">Styled Chat</a> config
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
    private static Config CONFIG;

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

        CONFIG = config;
        return true;
    }
}
