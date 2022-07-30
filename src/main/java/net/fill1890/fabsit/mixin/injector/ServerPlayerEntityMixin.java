package net.fill1890.fabsit.mixin.injector;

import net.fill1890.fabsit.config.ConfigManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    // when a player disconnects remove them from the list of supported players
    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void removeFromConfig(CallbackInfo ci) {
        ConfigManager.loadedPlayers.remove(this.networkHandler.connection.getAddress());
    }
}
