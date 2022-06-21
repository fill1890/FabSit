package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fill1890.fabsit.command.LayCommand;
import net.fill1890.fabsit.command.ReloadConfigCommand;
import net.fill1890.fabsit.command.SitCommand;
import net.fill1890.fabsit.command.SpinCommand;
import net.fill1890.fabsit.config.Config;
import net.fill1890.fabsit.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// TODO: refactor out
		CommandRegistrationCallback.EVENT.register(SitCommand::register);
		CommandRegistrationCallback.EVENT.register(LayCommand::register);
		CommandRegistrationCallback.EVENT.register(SpinCommand::register);
		CommandRegistrationCallback.EVENT.register(ReloadConfigCommand::register);

		if(!ConfigManager.loadConfig()) {
			LOGGER.warn("FabSit config not loaded! Using default settings");
		}
	}
}
