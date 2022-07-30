package net.fill1890.fabsit.entity;

import com.mojang.authlib.GameProfile;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.util.Messages;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

import static net.fill1890.fabsit.mixin.accessor.PlayerEntityAccessor.getLEFT_SHOULDER_ENTITY;
import static net.fill1890.fabsit.mixin.accessor.PlayerEntityAccessor.getRIGHT_SHOULDER_ENTITY;

/**
 * The PoseManagerEntity provides an interface to a variety of posing actions, currently:
 * <pre>entity.Pose.SITTING</pre>
 * <pre>entity.Pose.LAYING</pre>
 * <pre>entity.Pose.SPINNING</pre>
 * <br>
 * The manager spawns in invisible armour stand for the player to ride to sit
 * <br>
 * If needed, the player will then be made invisible and an NPC spawned to pose instead
 */
public class PoseManagerEntity extends ArmorStandEntity {
    public static final String ENTITY_ID = "pose_manager";

    // has the seat been used - checked for removing later
    private boolean used = false;
    // has the action bar status been sent? (needs to be delayed until after addPassenger has executed)
    private boolean statusSent = false;
    private final Pose pose;
    // visible npc for posing (if needed)
    private PosingEntity poser;

    private final BlockPos pos;

    // block ticking during removal
    // TODO: figure out how to remove this, was here to fix a bug
    protected boolean killing;

    protected Position position;

    public PoseManagerEntity(Vec3d pos, Pose pose, ServerPlayerEntity player, Position position) {
        // create a new armour stand at the appropriate height
        //super(player.getWorld(), pos.x, pos.y - 1.6, pos.z);
        super(FabSit.POSER_ENTITY_TYPE, player.getWorld());
        //super(player.getWorld(), pos.x, pos.y, pos.z);
        FabSit.LOGGER.info("Chair at " + pos);
        this.setPosition(pos.x, pos.y - 0.75, pos.z);

        this.setInvisible(true);
        this.setInvulnerable(true);
        this.setCustomName(Text.of("FABSEAT"));
        this.setNoGravity(true);
        this.setYaw(player.getYaw()); // TODO: test this properly

        this.pos = new BlockPos(pos);
        this.position = position;

        // if the pose is more complex than sitting, create a posing npc
        if(pose == Pose.LAYING || pose == Pose.SPINNING) {
            // copy player game profile with a random uuid
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getEntityName());
            gameProfile.getProperties().putAll(player.getGameProfile().getProperties());

            if(pose == Pose.LAYING) this.poser = new LayingEntity(player, gameProfile);
            if(pose == Pose.SPINNING) this.poser = new SpinningEntity(player, gameProfile);
        }

        this.pose = pose;
    }

    public PoseManagerEntity(EntityType<? extends PoseManagerEntity> entityType, World world) {
        super(entityType, world);

        FabSit.LOGGER.info("called generic seat constructor");

        // if this is called directly it's probably because it's after a server start
        // we don't have position or pose info so we just silently fail
        //this.kill();

        // update: we'll set uhhhh idk fields
        this.position = Position.ON_BLOCK;
        this.pose = Pose.SITTING;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);

        // if the pose is npc-based, hide the player when initiated
        if(this.pose == Pose.LAYING || this.pose == Pose.SPINNING) {
            passenger.setInvisible(true);

            // update shoulder entities
            // parrots and such
            // TODO: this should probably be updated in PosingEntity
            this.poser.getDataTracker().set(getLEFT_SHOULDER_ENTITY(), passenger.getDataTracker().get(getLEFT_SHOULDER_ENTITY()));
            this.poser.getDataTracker().set(getRIGHT_SHOULDER_ENTITY(), passenger.getDataTracker().get(getRIGHT_SHOULDER_ENTITY()));
            passenger.getDataTracker().set(getLEFT_SHOULDER_ENTITY(), new NbtCompound());
            passenger.getDataTracker().set(getRIGHT_SHOULDER_ENTITY(), new NbtCompound());
        }

        used = true;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        if(ConfigManager.getConfig().centre_on_blocks || position == Position.IN_BLOCK)
            ConfigManager.occupiedBlocks.remove(this.getBlockPos());

        // if the pose was npc-based, show the player again when exited
        if(this.pose == Pose.LAYING || this.pose == Pose.SPINNING) {
            passenger.setInvisible(false);

            // replace shoulder entities
            passenger.getDataTracker().set(getLEFT_SHOULDER_ENTITY(), this.poser.getDataTracker().get(getLEFT_SHOULDER_ENTITY()));
            passenger.getDataTracker().set(getRIGHT_SHOULDER_ENTITY(), this.poser.getDataTracker().get(getRIGHT_SHOULDER_ENTITY()));
            this.poser.getDataTracker().set(getLEFT_SHOULDER_ENTITY(), new NbtCompound());
            this.poser.getDataTracker().set(getRIGHT_SHOULDER_ENTITY(), new NbtCompound());
        }
    }

    public void animate(int id) {
        if(this.pose == Pose.LAYING || this.pose == Pose.SPINNING) {
            poser.animate(id);
        }
    }

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }

    @Override
    public boolean collides() {
        return false;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return new Vec3d(this.getX(), this.getY() + 1.6, this.getZ());
    }

    @Override
    public void kill() {
        this.killing = true;

        // if the pose was npc-based, remove the npc
        if(poser != null) {
            poser.destroy();
        }

        super.kill();
    }

    @Override
    public void tick() {
        if(this.killing) return;

        // kill when the player stops posing
        if(used && getPassengerList().size() < 1) { this.kill(); return; }

        // rotate the armour stand with the player so the player's legs line up
        if(this.getFirstPassenger() instanceof ServerPlayerEntity player) {
            this.setYaw(player.getHeadYaw());

            // send the action bar status if it hasn't been sent yet
            if(!this.statusSent) {
                if(ConfigManager.getConfig().enable_messages.action_bar)
                    player.sendMessage(Messages.getPoseStopMessage(player, this.pose), true);

                this.statusSent = true;
            }
        }

        BlockState sittingBlock = getEntityWorld().getBlockState(switch(this.position){
            case IN_BLOCK -> this.getBlockPos();
            case ON_BLOCK -> this.getBlockPos().down();
        });

        if(sittingBlock.isAir()) { this.kill(); return; }

        // if pose is npc-based, update players with npc info
        if(this.pose == Pose.LAYING || this.pose == Pose.SPINNING) {
            poser.sendUpdates();
        }

        super.tick();
    }
}
