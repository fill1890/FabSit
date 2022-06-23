package net.fill1890.fabsit.mixin;

import net.fill1890.fabsit.config.ConfigManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    // when a player disconnects remove them from the list of supported players
    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void removeFromConfig(CallbackInfo ci) {
        ConfigManager.loadedPlayers.remove((ServerPlayerEntity) (Object) this);
    }
}
