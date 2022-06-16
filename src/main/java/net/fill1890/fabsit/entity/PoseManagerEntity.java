package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class PoseManagerEntity extends ArmorStandEntity {
    private boolean used = false;
    private Pose pose;
    private PosingEntity poser;

    public PoseManagerEntity(World world, Vec3d pos, float yaw, Pose pose, ServerPlayerEntity player) {
        // TODO: no magic numbers
        super(world, pos.x, pos.y - 1.6, pos.z);

        this.setInvisible(true);
        this.setInvulnerable(true);
        this.setCustomName(Text.of("FABSEAT"));
        this.setNoGravity(true);
        this.setYaw(yaw);

        if(pose == Pose.LAYING) {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getEntityName());
            gameProfile.getProperties().putAll(player.getGameProfile().getProperties());

            this.poser = new LayingEntity(player, gameProfile);
        }

        this.pose = pose;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);

        if(this.pose == Pose.LAYING) {
            passenger.setInvisible(true);
            // TODO: update NBT compound data?
            // TODO: update visible equipment?
        }

        used = true;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        if(this.pose == Pose.LAYING) {
            passenger.setInvisible(false);
            // TODO: restorations once implemented
        }
    }

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }

    @Override
    public boolean collides() {
        return false;
    }

    @Override
    public void kill() {
        if(poser != null) {
            //poser.destroy();
        }

        super.kill();
    }

    @Override
    public void tick() {
        if(used && getPassengerList().size() < 1) { this.kill(); }

        ServerPlayerEntity player = (ServerPlayerEntity) this.getFirstPassenger();
        if(player != null) {
            this.setYaw(player.getHeadYaw());
        }

        BlockState sittingBlock = getEntityWorld().getBlockState(new BlockPos(getPos()).up());
        if(sittingBlock.isAir()) { kill(); }

        if(this.pose == Pose.LAYING) {
            poser.sendUpdates();
        }

        super.tick();
    }
}
