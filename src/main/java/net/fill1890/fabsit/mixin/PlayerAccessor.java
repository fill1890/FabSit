package net.fill1890.fabsit.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerAccessor {
    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> getPLAYER_MODEL_PARTS() {
        throw new AssertionError();
    }

    @Accessor("MAIN_ARM")
    static TrackedData<Byte> getMAIN_ARM() {
        throw new AssertionError();
    }
}
