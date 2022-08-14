package net.fill1890.fabsit.mixin.accessor;

import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Updater for fields of EntitySpawnS2CPacket
 */
@Mixin(EntitySpawnS2CPacket.class)
public interface EntitySpawnPacketAccessor {
    /**
     * Modify type ID of entity
     *
     * Used to fake out pose manager packets for cross-compatibility between vanilla and fabsit clients
     *
     * @param type new entity type
     */
    @Mutable
    @Accessor("entityTypeId")
    void setEntityTypeId(EntityType<?> type);

    /**
     * Modify y-position of entity
     *
     * Used to adjust y-positions of entities for consistency between vanilla and fabsit clients
     *
     * @param y new position
     */
    @Mutable
    @Accessor("y")
    void setY(double y);
}
