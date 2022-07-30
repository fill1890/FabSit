package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySpawnS2CPacket.class)
public interface EntitySpawnPacketAccessor {
    @Mutable
    @Accessor("entityTypeId")
    void setEntityTypeId(EntityType<?> type);

    @Mutable
    @Accessor("y")
    void setY(double y);
}
