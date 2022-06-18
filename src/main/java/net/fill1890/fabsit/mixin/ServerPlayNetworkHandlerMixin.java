package net.fill1890.fabsit.mixin;

import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    // Hijack client -> server animation packets
    // If the packet is an animation and the player is posing, transmit the same animation
    // from the posing npc
    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void copyHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if(this.player.hasVehicle()) {
            if(this.player.getVehicle() instanceof PoseManagerEntity poseManager) {
                if(packet.getHand() == Hand.MAIN_HAND) {
                    poseManager.animate(EntityAnimationS2CPacket.SWING_MAIN_HAND);
                } else {
                    poseManager.animate(EntityAnimationS2CPacket.SWING_OFF_HAND);
                }
            }
        }
    }
}
