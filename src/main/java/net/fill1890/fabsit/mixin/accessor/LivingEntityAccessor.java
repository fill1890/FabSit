package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("SLEEPING_POSITION")
    static TrackedData<Optional<BlockPos>> getSLEEPING_POSITION() {
        throw new AssertionError();
    }

    @Accessor("LIVING_FLAGS")
    static TrackedData<Byte> getLIVING_FLAGS() { throw new AssertionError(); }
}
