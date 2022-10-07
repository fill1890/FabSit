package net.fill1890.fabsit.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fill1890.fabsit.FabSit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ChairEntity extends Entity implements PolymerEntity {
    public static final String ENTITY_ID = "entity_chair";
    public static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(0.01F, 1.00F);

    public ChairEntity(EntityType<ChairEntity> type, World world) {
        super(type, world);
    }

    public static EntityType<ChairEntity> register() {
        return Registry.register(
                Registry.ENTITY_TYPE,
                new Identifier(FabSit.MOD_ID, ENTITY_ID),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, ChairEntity::new)
                                       .dimensions(DIMENSIONS)
                                       .build()
        );
    }
        @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    protected void initDataTracker() {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
