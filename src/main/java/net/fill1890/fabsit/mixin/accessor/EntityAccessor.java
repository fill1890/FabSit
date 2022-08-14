package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Access properties of Entity.class
 */
@Mixin(Entity.class)
public interface EntityAccessor {
    /**
     * Accessor for the NBT data for an entity pose
     *
     * Used to get the NBT location for setting a specific pose
     *
     * @return NBT pose data
     */
    @Accessor("POSE")
    static TrackedData<EntityPose> getPOSE() {
        throw new AssertionError();
    }
}
