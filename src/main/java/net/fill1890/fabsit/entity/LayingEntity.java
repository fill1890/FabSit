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

import static net.fill1890.fabsit.mixin.accessor.EntityAccessor.getPOSE;
import static net.fill1890.fabsit.mixin.accessor.LivingEntityAccessor.getSLEEPING_POSITION;

/**
 * Laying entity
 * <br>
 * Subclasses posing entity and implements a sleeping pose
 * <br>
 * Note that to do this, a bed is inserted into the bottom of the world (bedrock in the overworld or
 * nether, void in the end) client-side and set as the poser's bed. This is packet-based only
 * and does not affect the world server-side, and is reset when the pose is left
 */
public class LayingEntity extends PosingEntity {
    // replace a block with a bed
    private final BlockUpdateS2CPacket addBedPacket;
    // revert bed replacement
    private final BlockUpdateS2CPacket removeBedPacket;
    // teleport the poser to the correct location
    private final EntityPositionS2CPacket teleportPoserPacket;

    public LayingEntity(ServerPlayerEntity player, GameProfile profile) {
        super(player, profile);

        // set sleeping pose; mixin is again used to access entity data
        this.getDataTracker().set(getPOSE(), EntityPose.SLEEPING);

        // lowest possible block to put the bed on (minimal interference)
        int worldBottom = this.getEntityWorld().getDimension().minY();
        BlockPos bedPos = new BlockPos(this.getX(), worldBottom, this.getZ());
        // set the sleeping position of the poser to the bed
        this.getDataTracker().set(getSLEEPING_POSITION(), Optional.of(bedPos));

        // get the top half of a bed to replace the old block with
        BlockState bed = Blocks.WHITE_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD);
        // save the old block to restore it later
        BlockState old = this.getEntityWorld().getBlockState(bedPos);

        // update bed facing direction to match player
        bed = bed.with(BedBlock.FACING, this.initialDirection.getOpposite());

        // raise pose position to lie on the ground rather than in it
        this.setPosition(this.getX(), this.getY() + 0.1, this.getZ());

        this.addBedPacket = new BlockUpdateS2CPacket(bedPos, bed);
        this.removeBedPacket = new BlockUpdateS2CPacket(bedPos, old);
        // teleport the poser from the bed to the player, as the poser
        // spawns on the bed (mojang moment)
        this.teleportPoserPacket = new EntityPositionS2CPacket(this);
        // refresh metadata so the bed is assigned correctly
        this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker().getChangedEntries());

    }

    @Override
    public void sendUpdates() {
        super.sendUpdates();

        this.addingPlayers.forEach(p -> {
            // add the bed to the world
            p.networkHandler.sendPacket(addBedPacket);
            // refresh metadata now that the bed exists
            p.networkHandler.sendPacket(trackerPoserPacket);
            // teleport the poser to the surface
            p.networkHandler.sendPacket(teleportPoserPacket);
        });

        // reset bed blocks after posing
        this.removingPlayers.forEach(p -> p.networkHandler.sendPacket(removeBedPacket));
    }

    @Override
    public void destroy() {
        this.updatingPlayers.forEach(p -> p.networkHandler.sendPacket(removeBedPacket));

        super.destroy();
    }
}
