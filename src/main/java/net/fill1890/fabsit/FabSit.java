package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.util.Commands;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// packet channel for communication
	public static final Identifier fabsitChannel = new Identifier("fabsit:check_loaded");

	@Override
	public void onInitialize() {
		Commands.register();

		if(!ConfigManager.loadConfig()) {
			LOGGER.warn("FabSit config not loaded! Using default settings");
		}

		LOGGER.info("FabSit loaded");
	}
}
