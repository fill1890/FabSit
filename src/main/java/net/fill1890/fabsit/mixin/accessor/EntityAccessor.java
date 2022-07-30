package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("POSE")
    static TrackedData<EntityPose> getPOSE() {
        throw new AssertionError();
    }
}
