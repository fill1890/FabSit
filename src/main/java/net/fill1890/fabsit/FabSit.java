package net.fill1890.fabsit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.ChairEntity;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.error.LoadConfigException;
import net.fill1890.fabsit.util.Commands;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabSit implements ModInitializer {

	// mod info
	public static final String MOD_ID = "fabsit";

	public static final EntityType<ChairEntity> CHAIR_ENTITY_TYPE = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MOD_ID, ChairEntity.ENTITY_ID),
			FabricEntityTypeBuilder.<ChairEntity>create(SpawnGroup.MISC, ChairEntity::new).dimensions(ChairEntity.DIMENSIONS).build()
	);
	public static final EntityType<PoseManagerEntity> POSER_ENTITY_TYPE = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier(MOD_ID, PoseManagerEntity.ENTITY_ID),
			FabricEntityTypeBuilder.<PoseManagerEntity>create(SpawnGroup.MISC, PoseManagerEntity::new).dimensions(new EntityDimensions(0.5F, 1.975F, true)).build()
	);

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

		FabricDefaultAttributeRegistry.register(POSER_ENTITY_TYPE, ArmorStandEntity.createLivingAttributes());
		FabricDefaultAttributeRegistry.register(CHAIR_ENTITY_TYPE, LivingEntity.createLivingAttributes());
	}
}
