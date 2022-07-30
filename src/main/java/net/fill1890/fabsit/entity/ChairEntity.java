package net.fill1890.fabsit.entity;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.World;

public class ChairEntity extends ArmorStandEntity {
    public static final String ENTITY_ID = "entity_chair";
    public static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(0.01F, 1.00F);

    public ChairEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public ChairEntity(PoseManagerEntity poseManager) {
        super(poseManager.world, poseManager.getX(), poseManager.getY(), poseManager.getZ());

        this.copyFrom(poseManager);
    }

    public ChairEntity(EntityType<ChairEntity> type, World world) {
        super(type, world);
    }
}
