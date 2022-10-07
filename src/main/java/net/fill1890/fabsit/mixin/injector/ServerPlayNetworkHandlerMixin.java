package net.fill1890.fabsit.mixin.injector;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.mixin.accessor.EntitySpawnPacketAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hijack the network handler for various reasons
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    
    /**
     * Listen for player hand swings
     * <br>
     * If the player is currently posing and has a posing NPC, transmit a swing packet to nearby players
     * <br>
     * @param packet passed from mixin function
     * @param ci mixin callback info
     */
    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void copyHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        // if player is currently posing
        if(this.player.hasVehicle() && this.player.getVehicle() instanceof PoseManagerEntity poseManager) {
            // animate if need be
            poseManager.animate(switch (packet.getHand()) {
                case MAIN_HAND -> EntityAnimationS2CPacket.SWING_MAIN_HAND;
                case OFF_HAND -> EntityAnimationS2CPacket.SWING_OFF_HAND;
            });
        }
    }
}
