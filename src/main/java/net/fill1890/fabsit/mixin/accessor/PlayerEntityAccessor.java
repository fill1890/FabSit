package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> getPLAYER_MODEL_PARTS() {
        throw new AssertionError();
    }

    @Accessor("MAIN_ARM")
    static TrackedData<Byte> getMAIN_ARM() {
        throw new AssertionError();
    }

    @Accessor("LEFT_SHOULDER_ENTITY")
    static TrackedData<NbtCompound> getLEFT_SHOULDER_ENTITY() { throw new AssertionError(); }

    @Accessor("RIGHT_SHOULDER_ENTITY")
    static TrackedData<NbtCompound> getRIGHT_SHOULDER_ENTITY() { throw new AssertionError(); }
}
