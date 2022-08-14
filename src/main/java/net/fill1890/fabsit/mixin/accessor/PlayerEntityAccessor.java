package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for fields of PlayerEntity
 */
@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    /**
     * Get NBT location of skin layer data to match skin layers for posers
     *
     * @return NBT location of skin layer data
     */
    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> getPLAYER_MODEL_PARTS() {
        throw new AssertionError();
    }

    /**
     * @return NBT location of main arm selector
     */
    @Accessor("MAIN_ARM")
    static TrackedData<Byte> getMAIN_ARM() {
        throw new AssertionError();
    }

    /**
     * Get NBT location of the left shoulder entity, for syncing parrots with posers
     *
     * @return NBT location of shoulder entity
     */
    @Accessor("LEFT_SHOULDER_ENTITY")
    static TrackedData<NbtCompound> getLEFT_SHOULDER_ENTITY() { throw new AssertionError(); }

    /**
     * Get NBT location of the right shoulder entity, for syncing parrots with posers
     *
     * @return NBT location of shoulder entity
     */
    @Accessor("RIGHT_SHOULDER_ENTITY")
    static TrackedData<NbtCompound> getRIGHT_SHOULDER_ENTITY() { throw new AssertionError(); }
}
