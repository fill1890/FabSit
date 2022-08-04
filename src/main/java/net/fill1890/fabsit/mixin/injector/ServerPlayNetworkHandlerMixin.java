package net.fill1890.fabsit.mixin.injector;

import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.mixin.accessor.EntitySpawnPacketAccessor;
import net.minecraft.class_7648;
import net.minecraft.entity.EntityType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
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

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final public ClientConnection connection;

    @Shadow public abstract void sendPacket(Packet<?> packet, @Nullable class_7648 arg);

    // Hijack client -> server animation packets
    // If the packet is an animation and the player is posing, transmit the same animation
    // from the posing npc
    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void copyHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if(this.player.hasVehicle()) {
            if(this.player.getVehicle() instanceof PoseManagerEntity poseManager) {
                poseManager.animate(switch (packet.getHand()) {
                    case MAIN_HAND -> EntityAnimationS2CPacket.SWING_MAIN_HAND;
                    case OFF_HAND -> EntityAnimationS2CPacket.SWING_OFF_HAND;
                });
            }
        }
    }

    // hijack server -> client spawn packets
    // if spawning a posing entity, change the type
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V", at = @At("HEAD"), cancellable = true)
    private void fakeChair(Packet<?> packet, class_7648 listener, CallbackInfo ci) {

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

            sendPacket(sp, listener);
            ci.cancel();
        }
    }
}
