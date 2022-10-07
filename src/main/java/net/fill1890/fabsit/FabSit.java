package net.fill1890.fabsit;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.ChairEntity;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.error.LoadConfigException;
import net.fill1890.fabsit.util.Commands;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	// mod info
	public static final String MOD_ID = "fabsit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Pose manager and chair entities
	public static final EntityType<PoseManagerEntity> RAW_CHAIR_ENTITY_TYPE = PoseManagerEntity.register();
	public static final EntityType<ChairEntity> CHAIR_ENTITY_TYPE = ChairEntity.register();

	// packet channel for checking if mod loaded
	public static final Identifier LOADED_CHANNEL = new Identifier(MOD_ID, "check_loaded");
	// packet channel for pose requests (keybinds etc.)
	public static final Identifier REQUEST_CHANNEL = new Identifier(MOD_ID, "request_pose");


	@Override
	public void onInitialize() {
		PolymerEntityUtils.registerType(CHAIR_ENTITY_TYPE, RAW_CHAIR_ENTITY_TYPE);
		
		Commands.register();

		FabricDefaultAttributeRegistry.register(RAW_CHAIR_ENTITY_TYPE, ArmorStandEntity.createLivingAttributes());

		try {
			ConfigManager.loadConfig();
		} catch(LoadConfigException ignored) {
			LOGGER.warn("FabSit config not loaded! Using default settings");
		}

		LOGGER.info("FabSit loaded");
	}
}
