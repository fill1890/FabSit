package net.fill1890.fabsit.mixin.injector;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.ChairEntity;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(RegistrySyncManager.class)
public abstract class RegistrySyncManagerMixin {
    private static final Identifier ENTITY_TYPE = new Identifier("minecraft", "entity_type");

    @Inject(
            method = "sendPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/fabricmc/fabric/impl/registry/sync/packet/RegistryPacketHandler;)V",
            at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/registry/sync/packet/RegistryPacketHandler;sendPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/Map;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void removeFromSync(
            ServerPlayerEntity player, RegistryPacketHandler handler, CallbackInfo ci, Map<Identifier, Object2IntMap<Identifier>> map
    ) {
        //System.out.println("injected with map " + map);
        //FabSit.LOGGER.info("registry injected with map " + map);
        FabSit.LOGGER.info("syncing for address: " + player.networkHandler.connection.getAddress());
        if(!ConfigManager.loadedPlayers.contains(player.networkHandler.connection.getAddress())) {
            FabSit.LOGGER.info("Player doesn't have FabSit loaded - scrubbing registry");

            map.get(ENTITY_TYPE).removeInt(new Identifier(FabSit.MOD_ID, ChairEntity.ENTITY_ID));
            map.get(ENTITY_TYPE).removeInt(new Identifier(FabSit.MOD_ID, PoseManagerEntity.ENTITY_ID));
        }
    }

}
