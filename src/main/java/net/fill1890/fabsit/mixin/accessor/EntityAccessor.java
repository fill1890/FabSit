package net.fill1890.fabsit.mixin.accessor;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;


/**
 * Access properties of Entity.class
 */
@Mixin(Entity.class)
public interface EntityAccessor {
    /**
     * Accessor for the NBT data for an entity pose
     * <p>
     * Used to get the NBT location for setting a specific pose
     *
     * @return NBT pose data
     */
    @Accessor("POSE")
    static TrackedData<EntityPose> getPOSE() {
        throw new AssertionError();
    }
    
    @Accessor("NAME_VISIBLE")
    static TrackedData<Boolean> getNAME_VISIBLE() {
        throw new AssertionError();
    }
    
    @Accessor("CUSTOM_NAME")
    static TrackedData<Optional<Text>> getCUSTOM_NAME() {
        throw new AssertionError();
    }
}
