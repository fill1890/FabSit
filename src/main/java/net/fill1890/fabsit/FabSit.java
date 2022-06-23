package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.error.LoadConfigException;
import net.fill1890.fabsit.util.Commands;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	// mod info
	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// packet channel for checking if mod loaded
	public static final Identifier LOADED_CHANNEL = new Identifier("fabsit:check_loaded");
	// packet channel for pose requests (keybinds etc.)
	public static final Identifier REQUEST_CHANNEL = new Identifier("fabsit:request_pose");

	@Override
	public void onInitialize() {
		Commands.register();

		try {
			ConfigManager.loadConfig();
		} catch(LoadConfigException ignored) {
			LOGGER.warn("FabSit config not loaded! Using default settings");
		}

		LOGGER.info("FabSit loaded");
	}
}
