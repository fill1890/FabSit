package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static net.fill1890.fabsit.mixin.EntityAccessor.getPOSE;
import static net.fill1890.fabsit.mixin.LivingEntityAccessor.getSLEEPING_POSITION;

public class LayingEntity extends PosingEntity {
    private final BlockUpdateS2CPacket addBedPacket;
    private final BlockUpdateS2CPacket removeBedPacket;
    private final EntityPositionS2CPacket teleportPoserPacket;

    public LayingEntity(ServerPlayerEntity player, GameProfile profile) {
        super(player, profile);

        this.getDataTracker().set(getPOSE(), EntityPose.SLEEPING);

        int worldBottom = this.getEntityWorld().getDimension().minY();
        BlockPos bedPos = new BlockPos(this.getX(), worldBottom, this.getZ());
        this.getDataTracker().set(getSLEEPING_POSITION(), Optional.of(bedPos));

        BlockState bed = Blocks.WHITE_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD);
        BlockState old = this.getEntityWorld().getBlockState(bedPos);

        this.addBedPacket = new BlockUpdateS2CPacket(bedPos, bed);
        this.removeBedPacket = new BlockUpdateS2CPacket(bedPos, old);
        this.teleportPoserPacket = new EntityPositionS2CPacket(this);
        this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker(), false);
    }

    @Override
    public void sendUpdates() {
        // Update add & remove lists, any generic updates
        super.sendUpdates();

        this.addingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(addBedPacket);
            p.networkHandler.sendPacket(trackerPoserPacket);
            p.networkHandler.sendPacket(teleportPoserPacket);
        });

        this.removingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(removeBedPacket);
        });
    }

    @Override
    public void destroy() {
        this.updatingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(removeBedPacket);
        });

        super.destroy();
    }
}
