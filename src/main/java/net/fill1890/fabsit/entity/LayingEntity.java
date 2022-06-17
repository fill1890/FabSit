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
import net.minecraft.util.math.Direction;

import java.util.Optional;

import static net.fill1890.fabsit.mixin.EntityAccessor.getPOSE;
import static net.fill1890.fabsit.mixin.LivingEntityAccessor.getSLEEPING_POSITION;

/**
 * Laying entity
 *
 * Subclasses posing entity and implements a sleeping pose
 *
 * Note that to do this, a bed is inserted into the bottom of the world (bedrock in the overworld or
 *  nether, void in the end) client-side and set as the poser's bed. This is packet-based only
 * and does not affect the world server-side, and is reset when the pose is left
 */
public class LayingEntity extends PosingEntity {
    private final BlockUpdateS2CPacket addBedPacket;
    private final BlockUpdateS2CPacket removeBedPacket;
    private final EntityPositionS2CPacket teleportPoserPacket;

    // TODO: line up bed facing direction with player
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
        Direction direction = this.getCardinal(player.getHeadYaw());
        bed = bed.with(BedBlock.FACING, direction.getOpposite());

        // raise pose position to lie on the ground rather than in it
        this.setPosition(player.getX(), player.getY() + 0.1, player.getZ());

        // change the new bed block to a bed
        this.addBedPacket = new BlockUpdateS2CPacket(bedPos, bed);
        // reset to the original block
        this.removeBedPacket = new BlockUpdateS2CPacket(bedPos, old);
        // teleport the poser from the bed to the player, as the poser
        // spawns on the bed (mojang moment)
        this.teleportPoserPacket = new EntityPositionS2CPacket(this);
        // refresh metadata so the bed is assigned correctly
        this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker(), false);

    }

    @Override
    public void sendUpdates() {
        super.sendUpdates();

        this.addingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(addBedPacket);
            // refresh metadata now that the bed exists
            p.networkHandler.sendPacket(trackerPoserPacket);
            p.networkHandler.sendPacket(teleportPoserPacket);
        });

        this.removingPlayers.forEach(p -> p.networkHandler.sendPacket(removeBedPacket));
    }

    @Override
    public void destroy() {
        this.updatingPlayers.forEach(p -> p.networkHandler.sendPacket(removeBedPacket));

        super.destroy();
    }
}
