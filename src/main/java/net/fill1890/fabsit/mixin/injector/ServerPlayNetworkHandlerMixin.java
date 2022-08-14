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

    @Shadow @Final public ClientConnection connection;

    @Shadow public abstract void sendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks);

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

    /**
     * Hijack server -> client spawn packets and server -> client attribute updates
     * <br>
     * Spawn packets: If the server is trying to spawn a pose manager, overwrite with either an armor stand or a chair
     * depending on whether the client has fabsit loaded
     * <br>
     * Attribute updates: If the client has fabsit, error will be dumped in logs if we try to apply armor stand
     * attributes to a non-living entity, so block them
     *
     * @param packet passed from mixin function
     * @param callbacks passed from mixin function
     * @param ci mixin callback info
     */
    //@Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void fakeChair(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {

        // check for spawn packets, then spawn packets for the poser
        if(packet instanceof EntitySpawnS2CPacket sp && sp.getEntityTypeId() == FabSit.RAW_CHAIR_ENTITY_TYPE) {

            // if fabsit loaded, replace with the chair entity to hide horse hearts
            if (ConfigManager.loadedPlayers.contains(connection.getAddress())) {
                ((EntitySpawnPacketAccessor) sp).setEntityTypeId(FabSit.CHAIR_ENTITY_TYPE);
                ((EntitySpawnPacketAccessor) sp).setY(sp.getY() + 0.75);

            // if not just replace with an armour stand
            } else {
                ((EntitySpawnPacketAccessor) sp).setEntityTypeId(EntityType.ARMOR_STAND);
            }

            // send the updated packet
            sendPacket(sp, callbacks);
            // prevent further packet action
            ci.cancel();
        }

        // check for entity attribute packets, and block for clients with fabsit
        // clients spit an error into logs when we try to update a non-living entity with living attributes
        if(packet instanceof EntityAttributesS2CPacket ap) {
            Entity entity = player.getWorld().getEntityById(ap.getEntityId());
            if(entity == null) return;

            EntityType<?> type = entity.getType();
            if(type != FabSit.RAW_CHAIR_ENTITY_TYPE) return;

            // cancel packet if player has fabsit loaded
            if(ConfigManager.loadedPlayers.contains(connection.getAddress())) ci.cancel();
        }
    }
}
