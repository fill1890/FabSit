package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/**
 * Accessor for LivingEntity
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    /**
     * Get the NBT data location for the sleeping position to manually set it, for sleeping entity
     *
     * @return NBT location of sleeping position
     */
    @Accessor("SLEEPING_POSITION")
    static TrackedData<Optional<BlockPos>> getSLEEPING_POSITION() {
        throw new AssertionError();
    }

    /**
     * Get the NBT data location for living flags, to make posing entity spin
     *
     * @return NBT location of living flags
     */
    @Accessor("LIVING_FLAGS")
    static TrackedData<Byte> getLIVING_FLAGS() { throw new AssertionError(); }
}
