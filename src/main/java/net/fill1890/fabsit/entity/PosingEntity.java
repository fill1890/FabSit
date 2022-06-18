package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.fill1890.fabsit.mixin.PlayerEntityAccessor.getMAIN_ARM;
import static net.fill1890.fabsit.mixin.PlayerEntityAccessor.getPLAYER_MODEL_PARTS;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;

/**
 * The PosingEntity class gives a generic entity for posing <br>
 * It is assumed that the entity will visually replace the player
 * <br>
 * As a super class, will handle creating an NPC, setting the skin,
 * spawning into the world and removing from the world, visually
 * removing and adding hotbar items
 * <br>
 * Do not spawn posing entities in directly as they are server only
 * and will not tick or die correctly; instead use a ticking manager
 * and call sendUpdates() and destroy()
 * <br>
 * TODO: head rotation
 */
public abstract class PosingEntity extends ServerPlayerEntity {
    // add poser to the tablist
    private final PlayerListS2CPacket addPoserPacket;
    // remove poser from the tablist
    private final PlayerListS2CPacket removePoserPacket;
    // spawn poser in the world
    private final PlayerSpawnS2CPacket spawnPoserPacket;
    // remove poser from the world
    private final EntitiesDestroyS2CPacket despawnPoserPacket;
    // send poser metadata
    protected EntityTrackerUpdateS2CPacket trackerPoserPacket;

    // player posing
    protected final ServerPlayerEntity player;
    // list of players that need the poser removed from the tablist
    private final List<net.minecraft.util.Pair<ServerPlayerEntity, Integer>> delayedRemoves = new ArrayList<>();

    Direction initialDirection;

    // Set of players currently being added; use for initial setup
    protected final Set<ServerPlayerEntity> addingPlayers = new HashSet<>();
    // Set of players currently aware of poser; use for continuing updates/final removal
    protected final Set<ServerPlayerEntity> updatingPlayers = new HashSet<>();
    // Set of players currently being removed; use for resetting world state
    protected final Set<ServerPlayerEntity> removingPlayers = new HashSet<>();

    /**
     * Create a new EntityPoser
     *
     * @param player player to base poser on
     * @param gameProfile game profile of player
     */
    public PosingEntity(ServerPlayerEntity player, GameProfile gameProfile) {
        super(player.server, player.getWorld(), gameProfile, null);

        this.player = player;

        // poser shouldn't take damage
        // TODO: should this be changed to make the player invulnerable? so hit boxes are correct
        this.setInvulnerable(true);

        // update the player skin - uses a mixin to access private fields of PlayerEntity and superclasses
        // TODO: skin outer layer missing?
        this.getDataTracker().set(getPLAYER_MODEL_PARTS(), player.getDataTracker().get(getPLAYER_MODEL_PARTS()));
        this.getDataTracker().set(getMAIN_ARM(), player.getDataTracker().get(getMAIN_ARM()));

        // set the poser position
        this.setPosition(player.getPos());

        // get the initial yaw normalised 0-360
        //this.initialYaw = Math.round(player.getHeadYaw()) + 180;

        this.initialDirection = getCardinal(player.getHeadYaw());

        // adds the poser to the tablist so minecraft shows the player
        this.addPoserPacket = new PlayerListS2CPacket(ADD_PLAYER, this);
        // remove the poser from the tablist
        this.removePoserPacket = new PlayerListS2CPacket(REMOVE_PLAYER, this);
        // spawn the poser
        this.spawnPoserPacket = new PlayerSpawnS2CPacket(this);
        // despawn the poser
        this.despawnPoserPacket = new EntitiesDestroyS2CPacket(this.getId());
        // update the poser metadata
        this.trackerPoserPacket = new EntityTrackerUpdateS2CPacket(this.getId(), this.getDataTracker(), false);
    }

    /**
     * Request the entity to send packet updates to nearby players
     * <br>
     * Will keep track of players close enough to register (<250 blocks) and
     * able to see the player; will send packets to add the player when they
     * are visible and remove the player when not visible
     * <br>
     * Subclasses should override and call super.sendUpdates() before performing
     * pose-specific packet updates
     */
    public void sendUpdates() {
        // reset player lists
        this.addingPlayers.clear();
        this.removingPlayers.clear();

        // get all players in the current world
        this.getWorld().getPlayers()
                .forEach(p-> {
                    // check if they're being updated, in range of the poser, and can see the poser
                    boolean updating = this.updatingPlayers.contains(p);
                    boolean inRange = p.getPos().isInRange(this.getPos(), 250);
                    boolean visible = p.canSee(this);
                    if(inRange && visible && !updating) {
                        // Is in range, but wasn't before now so add data
                        this.addingPlayers.add(p);
                        this.updatingPlayers.add(p);
                    } else if(!inRange && updating) {
                        // Not in range, but was before now so remove data
                        this.updatingPlayers.remove(p);
                        this.removingPlayers.add(p);
                    }
                });

        this.addingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(this.addPoserPacket);
            p.networkHandler.sendPacket(this.spawnPoserPacket);
            p.networkHandler.sendPacket(this.trackerPoserPacket);

            // delay removing player from the tablist so the skins are rendered
            delayedRemoves.add(new net.minecraft.util.Pair<>(p, 0));
        });

        this.removingPlayers.forEach(p -> p.networkHandler.sendPacket(this.despawnPoserPacket));

        List<net.minecraft.util.Pair<ServerPlayerEntity, Integer>> removed = new ArrayList<>();
        this.delayedRemoves.forEach(p -> {
            if(p.getRight() >= 15) {
                // remove player from the tablist 15 ticks after being added
                p.getLeft().networkHandler.sendPacket(this.removePoserPacket);
                removed.add(p);
            } else {
                // increment the lifetime
                p.setRight(p.getRight() + 1);
            }
        });
        removed.forEach(this.delayedRemoves::remove);

        this.syncInventories();
        this.syncHeadYaw();
    }

    /**
     * Sync the NPC equipment with the given player
     * <br>
     * Will update visible equipment for both the NPC and player for all nearby
     * players, except the player being synced
     */
    protected void syncInventories() {
        this.syncInventories(this.player, true);
    }

    /**
     * Desync NPC equipment
     * <br>
     * Restores player equipment and removes from the NPC
     */
    protected void desyncInventories() {
        this.syncInventories(player, false);
    }

    protected void syncInventories(ServerPlayerEntity player, boolean playerHidden) {
        EntityEquipmentUpdateS2CPacket emptyPlayerPacket;
        EntityEquipmentUpdateS2CPacket emptyNpcPacket;
        EntityEquipmentUpdateS2CPacket equippedPlayerPacket;
        EntityEquipmentUpdateS2CPacket equippedNpcPacket;
        List<Pair<EquipmentSlot, ItemStack>> emptySlots = new ArrayList<>();
        List<Pair<EquipmentSlot, ItemStack>> playerSlots = new ArrayList<>();

        // set up a list of empty slots and a list of the player's equipped slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            emptySlots.add(new Pair<>(slot, ItemStack.EMPTY));
            playerSlots.add(new Pair<>(slot, player.getEquippedStack(slot)));
        }

        // set up empty & equipped packets for player & poser
        emptyPlayerPacket = new EntityEquipmentUpdateS2CPacket(player.getId(), emptySlots);
        emptyNpcPacket = new EntityEquipmentUpdateS2CPacket(this.getId(), emptySlots);
        equippedPlayerPacket = new EntityEquipmentUpdateS2CPacket(player.getId(), playerSlots);
        equippedNpcPacket = new EntityEquipmentUpdateS2CPacket(this.getId(), playerSlots);


        this.updatingPlayers.forEach(p -> {
            // ignore the player we're syncing with, so they can still modify inventory and stuff
            if(p != player) {
                // if player is hidden, send an empty player and equipped NPC
                p.networkHandler.sendPacket(playerHidden ? emptyPlayerPacket : emptyNpcPacket);
                p.networkHandler.sendPacket(playerHidden ? equippedNpcPacket : equippedPlayerPacket);
            }
        });

        this.removingPlayers.forEach(p -> {
            // when players are removed, restore
            p.networkHandler.sendPacket(emptyNpcPacket);
            p.networkHandler.sendPacket(equippedPlayerPacket);
        });
    }

    // TODO: finish implementation
    protected void syncHeadYaw() {
        if(player.headYaw != player.prevHeadYaw) {
            // yaw is usually from -180 to 180, with the break at north,
            // 0 at south, east at -90, and west at 90
            // so we take the head yaw, change the key (0) to the initial direction,
            // then adjust for the break so we have a clean measurement of
            // -180 to 180

            int yaw;
            if(initialDirection == Direction.SOUTH) {
                // already reasonable values (key at 0)
                yaw = Math.round(player.getHeadYaw());
            } else if(initialDirection == Direction.EAST) {
                // key at -90
                // becomes 0, 90, 180, 270/-90
                yaw = Math.round(player.getHeadYaw()) + 90;
                yaw = yaw > 180 ? yaw - 360 : yaw;
            } else if(initialDirection == Direction.WEST) {
                // key at 90
                // becomes 0, 90/-270, -180, -90
                yaw = Math.round(player.getHeadYaw()) - 90;
                yaw = yaw < -180 ? yaw + 360 : yaw;
            } else {
                // key at 180/-180; reverse and compensate
                yaw = Math.round(player.getHeadYaw());
                yaw = yaw > 0 ? yaw - 180 : yaw + 180;
            }

            // constrain to -30 to 80 degrees - seems to be correct, not sure why there's an offset
            yaw = Math.min(Math.max(yaw, -30), 80);

            // copied from gsit - something to do with the byte conversion?
            int newYaw = yaw * 256 / 360;

            EntitySetHeadYawS2CPacket headYawPacket = new EntitySetHeadYawS2CPacket(this, (byte) yaw);
            this.updatingPlayers.forEach(p -> p.networkHandler.sendPacket(headYawPacket));
        }
    }

    /**
     * Animate the poser
     *
     * @param id animation id from EntityAnimationS2CPacket
     */
    public void animate(int id) {
        if(id == EntityAnimationS2CPacket.SWING_MAIN_HAND || id == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            EntityAnimationS2CPacket animatePacket = new EntityAnimationS2CPacket(this, id);

            this.updatingPlayers.forEach(p -> p.networkHandler.sendPacket(animatePacket));
        }
    }

    /**
     * Get the cardinal direction for a given head yaw
     *
     * @param yaw yaw from -180 to 180 degrees
     * @return cardinal direction
     */
    public Direction getCardinal(float yaw) {
        if(yaw >= -45 && yaw <= 45) {
            return Direction.SOUTH;
        } else if(yaw > 45 && yaw <= 135) {
            return Direction.WEST;
        } else if(yaw >= -135 && yaw < -45) {
            return Direction.EAST;
        } else {
            return Direction.NORTH;
        }
    }

    /**
     * Destroy the posing entity
     * <br>
     * Call similarly to kill()
     * <br>
     * Subclasses should override and call super.destroy() after performing any pose-specific
     * breaking down
     */
    public void destroy() {
        this.desyncInventories();

        this.updatingPlayers.forEach(p -> {
            p.networkHandler.sendPacket(this.removePoserPacket);
            p.networkHandler.sendPacket(this.despawnPoserPacket);
        });
    }

    /**
     * kill() for this entity is rerouted to destroy() as it will not be spawned
     */
    @Override
    public void kill() {
        this.destroy();
    }

    /**
     * Entity should not be spawned server side
     */
    @Override
    public void onSpawn() {
        throw new RuntimeException("Posing entities should not be spawned!");
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