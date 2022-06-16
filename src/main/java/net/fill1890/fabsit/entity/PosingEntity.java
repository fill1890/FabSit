package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.fill1890.fabsit.mixin.PlayerAccessor.getMAIN_ARM;
import static net.fill1890.fabsit.mixin.PlayerAccessor.getPLAYER_MODEL_PARTS;
import static net.fill1890.fabsit.mixin.EntityAccessor.getPOSE;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;

public abstract class PosingEntity extends ServerPlayerEntity {
    private final PlayerListS2CPacket addPoserPacket;
    private final PlayerListS2CPacket removePoserPacket;
    private final PlayerSpawnS2CPacket spawnPoserPacket;
    private final EntitiesDestroyS2CPacket despawnPoserPacket;
    protected EntityTrackerUpdateS2CPacket trackerPoserPacket;


    protected final Set<ServerPlayerEntity> addingPlayers = new HashSet<>();
    protected final Set<ServerPlayerEntity> updatingPlayers = new HashSet<>();
    protected final Set<ServerPlayerEntity> removingPlayers = new HashSet<>();

    public PosingEntity(ServerPlayerEntity player, GameProfile gameProfile) {
        super(player.server, player.getWorld(), gameProfile, null);

        this.setCustomNameVisible(false); // doesnt do anything? hmm
        this.setInvulnerable(true); // double check this later

        this.getDataTracker().set(getPLAYER_MODEL_PARTS(), player.getDataTracker().get(getPLAYER_MODEL_PARTS()));
        this.getDataTracker().set(getMAIN_ARM(), player.getDataTracker().get(getMAIN_ARM()));

        this.setPosition(player.getPos());

        this.addPoserPacket = new PlayerListS2CPacket(ADD_PLAYER, this);
        this.removePoserPacket = new PlayerListS2CPacket(REMOVE_PLAYER, this);
        this.spawnPoserPacket = new PlayerSpawnS2CPacket(this);
        this.despawnPoserPacket = new EntitiesDestroyS2CPacket(this.getId());
        //this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker(), false);
    }

    public void sendUpdates() {
        this.addingPlayers.clear();
        this.removingPlayers.clear();

        this.getWorld().getPlayers()
                .forEach(p-> {
                    boolean updating = this.updatingPlayers.contains(p);
                    boolean inRange = p.getPos().isInRange(this.getPos(), 250);
                    boolean visible = p.canSee(this);
                    if(inRange && visible && !updating) { // TODO: test radius
                        // Is in range, but wasn't
                        this.addingPlayers.add(p);
                        this.updatingPlayers.add(p);
                    } else if(!inRange && updating) {
                        // Not in range, but was
                        this.updatingPlayers.remove(p);
                        this.removingPlayers.add(p);
                    }
                });

        this.addingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(this.addPoserPacket);
            p.networkHandler.sendPacket(this.spawnPoserPacket);
            p.networkHandler.sendPacket(this.trackerPoserPacket);
            p.networkHandler.sendPacket(this.removePoserPacket);
        });

        this.removingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(this.despawnPoserPacket);
        });
    }

    public void destroy() {
        this.updatingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(this.removePoserPacket);
            p.networkHandler.sendPacket(this.despawnPoserPacket);
        });
    }

    @Override
    public boolean collides() {
        return false;
    }

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }
}
