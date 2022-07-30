package net.fill1890.fabsit.mixin.injector;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
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

    @Shadow public abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener);

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

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void fakeChair(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if(packet instanceof EntitySpawnS2CPacket sp) {
            System.out.println("spawn packet hijacked: " + sp.getEntityTypeId());
            if(sp.getEntityTypeId() == FabSit.POSER_ENTITY_TYPE) {
                System.out.println("checking entity for address " + connection.getAddress());
                if (ConfigManager.loadedPlayers.contains(connection.getAddress())) {
                    System.out.println("address " + connection.getAddress() + " has fabsit, replacing entity");

                    //EntitySpawnPacketAccessor.setEntityTypeId(FabSitServer.CHAIR_ENTITY_TYPE);
                    sendPacket(new EntitySpawnS2CPacket(
                            sp.getId(),
                            sp.getUuid(),
                            sp.getX(),
                            sp.getY(),
                            sp.getZ(),
                            sp.getPitch(),
                            sp.getYaw(),
                            FabSit.CHAIR_ENTITY_TYPE,
                            sp.getEntityData(),
                            new Vec3d(sp.getVelocityX(), sp.getVelocityY(), sp.getVelocityZ()),
                            sp.getHeadYaw()), listener
                    );

                    ci.cancel();
                } else {
                    System.out.println("address " + connection.getAddress() + " does not have fabsit, replacing");

                    sendPacket(new EntitySpawnS2CPacket(
                            sp.getId(),
                            sp.getUuid(),
                            sp.getX(),
                            sp.getY(),
                            sp.getZ(),
                            sp.getPitch(),
                            sp.getYaw(),
                            EntityType.ARMOR_STAND,
                            sp.getEntityData(),
                            new Vec3d(sp.getVelocityX(), sp.getVelocityY(), sp.getVelocityZ()),
                            sp.getHeadYaw()), listener
                    );
                }
            }
        }
    }
}
