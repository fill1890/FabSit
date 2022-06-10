package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("FabSit finished loading");
	}
}
