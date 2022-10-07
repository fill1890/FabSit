package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.RaycastContext;
import org.lwjgl.system.MathUtil;

import static net.fill1890.fabsit.mixin.accessor.LivingEntityAccessor.getLIVING_FLAGS;

public class SpinningEntity extends PosingEntity {
    // pivot the poser to face vertically
    final EntityS2CPacket pivotPacket;

    public SpinningEntity(ServerPlayerEntity player, GameProfile gameProfile) {
        super(player, gameProfile);
        
        setPosition(getPos().add(Math.sin(player.getPitch()), 0, Math.cos(player.getPitch())));

        // set spinning state
        this.getDataTracker().set(getLIVING_FLAGS(), (byte) 0x04);
        // refresh data packet
        this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker(), true);

        this.pivotPacket = new EntityS2CPacket.RotateAndMoveRelative(this.getId(), (short) 0, (short) 0, (short) 0, (byte) 0, (byte) (-90.0f * 256.0f / 360.0f), true);
    }

    @Override
    public void sendUpdates() {
        super.sendUpdates();

        // rotate the poser to be spinning vertically
        this.addingPlayers.forEach(p -> p.networkHandler.sendPacket(this.pivotPacket));
    }

    @Override
    protected void syncHeadYaw() {
        // do nothing; no point since already rotating
        // also might mess up angle
    }
}
