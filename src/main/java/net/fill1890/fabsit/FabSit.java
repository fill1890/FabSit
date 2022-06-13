package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fill1890.fabsit.command.SitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(SitCommand::register);
		
		LOGGER.info("FabSit finished loading");
	}
}
