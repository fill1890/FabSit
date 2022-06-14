package net.fill1890.fabsit.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChairEntity extends ArmorStandEntity {
    private boolean used = false;

    public ChairEntity(World world, Vec3d pos, float yaw) {
        // TODO: no magic numbers
        super(world, pos.x, pos.y - 1.6, pos.z);

        this.setInvisible(true);
        this.setInvulnerable(true);
        this.setCustomName(Text.of("FABSEAT"));
        this.setNoGravity(true);
        this.setYaw(yaw);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        used = true;
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
    public void tick() {
        if(used && getPassengerList().size() < 1) { kill(); }

        ServerPlayerEntity player = (ServerPlayerEntity) this.getFirstPassenger();
        if(player != null) {
            this.setYaw(player.getHeadYaw());
        }

        BlockState sittingBlock = getEntityWorld().getBlockState(new BlockPos(getPos()).up());
        if(sittingBlock.isAir()) { kill(); }

        super.tick();
    }
}
